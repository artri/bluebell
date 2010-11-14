/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Rich Client.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bluebell.richclient.util;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.TransformedPredicate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.binding.value.support.ObservableEventList;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;

/**
 * An observable event list that manages <code>addAll</code> methods in an atomic way. So, a single event is raised
 * instead of one per single isolated new list element addition or removal.
 * 
 * @param <S>
 *            the type of list elements.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class AtomicObservableEventList<S> extends ObservableEventList {

    /**
     * Creates the atomic list given its source.
     * 
     * @param source
     *            the source list.
     */
    public AtomicObservableEventList(EventList<S> source) {

        super(source);
    }

    /**
     * {@inheritDoc}.
     * <p>
     * 
     * @see #doClear()
     */
    @Override
    public void clear() {

        final int startIndex = 0;
        final int endIndex = this.size() - 1;

        // [1] Force begin events (this will increase "event level" counts and delay commits after [3])
        this.beforeOperation(startIndex, endIndex, ListEvent.DELETE);
        try {
            // [2] At this point super should be called (without events being published), but "doClear" is employed
            // instead due to original implementation fails with this "hack"
            this.doClear();
        } catch (RuntimeException e) {
            throw e; // If an exception ocurred then rethrow it but restoring initial state [3]
        } finally {
            // [3] Force commits (this will decrease initially increased "event level" counts [1] and force commits)
            this.afterOperation(startIndex, endIndex, ListEvent.DELETE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(int index, @SuppressWarnings("rawtypes") Collection values) {

        Assert.notNull(values, "values");

        final int startIndex = index;
        final int endIndex = startIndex + values.size() - 1;

        // [1] Force begin events (this will increase "event level" counts and delay commits after [3])
        this.beforeOperation(startIndex, endIndex, ListEvent.INSERT);
        try {
            // [2] Call super (no event will be published)
            return super.addAll(index, values);
        } catch (RuntimeException e) {
            throw e; // If an exception ocurred then rethrow it but restoring initial state [3]
        } finally {
            // [3] Force commits (this will decrease initially increased "event level" counts [1] and force commits)
            this.afterOperation(startIndex, endIndex, ListEvent.INSERT);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method has been overriden for performance reasons.
     */
    @Override
    public String toString() {

        final String elementClazz = (!this.isEmpty()) ? this.get(0).getClass().getSimpleName() : "EMPTY";

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("size", this.getSize()).append(
                "elementClass", elementClazz).toString();
    }

    /**
     * Avoids later event been commited.
     * 
     * @param startIndex
     *            index at which to handle the first element.
     * @param endIndex
     *            index at which to handle the last element.
     * @param operationType
     *            the operation type, distinguish between<code>ListEvent.DELETE</code>, <code>ListEvent.UPDATE</code>
     *            and <code>ListEvent.INSERT</code>.
     */
    protected void beforeOperation(final int startIndex, final int endIndex, final int operationType) {

        if (endIndex < startIndex) {
            return;
        }

        // Begins an event (that allows nested events) for every event list at the chain.
        // This will increase "event level" count and delay commit.
        AtomicObservableEventList.forAllEventAssemblerDo((EventList<?>) this.source, new Predicate() {

            @Override
            public boolean evaluate(Object object) {

                final ListEventAssembler<?> updates = (ListEventAssembler<?>) object;

                switch (operationType) {
                    case ListEvent.DELETE:
                    case ListEvent.UPDATE:
                    case ListEvent.INSERT:
                    default:
                        updates.beginEvent(Boolean.TRUE);
                }

                return Boolean.TRUE;
            }
        });
    }

    /**
     * Force changes been commited.
     * <p>
     * 
     * @param startIndex
     *            index at which to handle the first element.
     * @param endIndex
     *            index at which to handle the last element.
     * 
     * @param operationType
     *            the operation type, distinguish between <code>ListEvent.DELETE</code>, <code>ListEvent.UPDATE</code>
     *            and <code>ListEvent.INSERT</code>.
     */
    protected void afterOperation(final int startIndex, final int endIndex, final int operationType) {

        if (endIndex < startIndex) {
            return;
        }

        // Commits an event for every event list in the chain.
        // This will decrease "event level" count and force commit.
        AtomicObservableEventList.forAllEventAssemblerDo((EventList<?>) this.source, new Predicate() {

            @SuppressWarnings("deprecation")
            @Override
            public boolean evaluate(Object object) {

                final ListEventAssembler<?> updates = (ListEventAssembler<?>) object;

                // Discard all amount of events and create a single one
                updates.discardEvent();
                Assert.state(updates.isEventEmpty(), "updates.isEventEmpty()");

                // (JAF), 20100428, I don't know how to avoid raising N events without calling deprecated methods
                updates.beginEvent();
                switch (operationType) {
                    case ListEvent.DELETE:
                        updates.addDelete(startIndex, endIndex);
                        break;
                    case ListEvent.UPDATE:
                        break;
                    case ListEvent.INSERT:
                        updates.addInsert(startIndex, endIndex);
                        break;
                    default:
                }
                updates.commitEvent();

                return Boolean.TRUE;
            }
        });
    }

    /**
     * Replacement for <code>super#clear()</code> when dealing with "hacked" implementation of {@link #clear()}. This is
     * needed due to the fact some <code>EventList</code> implementations (like <code>ThreadProxyEventList</code>)
     * employ an internal cache, that is not updated at the point of calling <code>super#clear()</code>, in such a case
     * calls to <code>#size()</code> will "lie".
     * <p>
     * So the following code:
     * 
     * <pre>
     * final int size = this.getSize();
     * 
     * for (int i = 0; i &lt; size; ++i) {
     *     this.remove(0);
     * }
     * </pre>
     * 
     * replaces:
     * 
     * <pre>
     * for (Iterator i = iterator(); i.hasNext();) {
     *     i.next();
     *     i.remove();
     * }
     * </pre>
     * <p>
     * Last one iterates all over the (non updated) list cache and this causes illegal index access.
     * 
     * @see #clear()
     */
    private void doClear() {

        final int size = this.getSize();

        for (int i = 0; i < size; ++i) {
            this.remove(0);
        }
    }

    /**
     * Evaluates the <em>event assembler</em> instance of every wrapped event list, including the one given as argument.
     * <p>
     * Stops after the first negative evaluation or at the end of the chain.
     * 
     * @param <Q>
     *            the type of the elements of the list.
     * 
     * @param eventList
     *            the event list.
     * @param predicate
     *            the predicate to evaluate.
     * @return <code>true</code> if every evaluation is positive and <code>false</code> in other case.
     * 
     * @see GlazedListsUtils#forAllDo(List, Predicate)
     */
    protected static <Q> Boolean forAllEventAssemblerDo(final List<Q> eventList, final Predicate predicate) {

        final Predicate transformedPredicate = TransformedPredicate.getInstance(//
                ObjectToFieldValueTransformer.getInstance("updates"), predicate);

        return GlazedListsUtils.forAllDo(eventList, transformedPredicate);
    }
}
