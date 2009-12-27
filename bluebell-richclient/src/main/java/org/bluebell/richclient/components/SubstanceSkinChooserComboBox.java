/**
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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

/**
 * 
 */
package org.bluebell.richclient.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.skin.SkinInfo;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.form.binding.Binding;
import org.springframework.richclient.form.binding.swing.SwingBindingFactory;
import org.springframework.util.Assert;

/**
 * Component capable of changing the current Substance Skin.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceSkinChooserComboBox extends AbstractForm {

    /**
     * Creates the component.
     */
    public SubstanceSkinChooserComboBox() {

        super();

        this.setFormModel(FormModelHelper.createFormModel(new SkinBean()));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected JComponent createFormControl() {

        // The backing form object, transformer and value change listener
        final SkinBean skin = (SkinBean) this.getFormObject();

        // Collect all the skins
        final Collection<SkinBean> skins = CollectionUtils.collect(SubstanceLookAndFeel.getAllSkins().entrySet(), skin);

        // Create the binding
        final SwingBindingFactory bindingFactory = (SwingBindingFactory) this.getBindingFactory();
        final Binding binding = bindingFactory.createBoundComboBox("skinBean", skins, "displayName");
        this.getFormModel().getValueModel(binding.getProperty()).addValueChangeListener(skin);

        return binding.getControl();
    }

    /**
     * Helper class used as:
     * <ul>
     * <li>Backing form object.
     * <li>Backing form property type.
     * <li>Collection element transformer.
     * <li>Property change listener.
     * </ul>
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    protected class SkinBean implements Transformer, PropertyChangeListener {

        /**
         * The property to bind.
         */
        private SkinBean skinBean;

        /**
         * The skin display name.
         */
        private String displayName;

        /**
         * The skin class name.
         */
        private String className;

        /**
         * Constructs the bean.
         */
        public SkinBean() {

            final SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();
            if (skin != null) {
                this.init(skin.getDisplayName(), skin.getClass().getName());
            }
        }

        /**
         * Constructs the bean given the skin display name and class.
         * 
         * @param displayName
         *            the display name.
         * @param className
         *            the class name.
         */
        public SkinBean(String displayName, String className) {

            this.init(displayName, className);
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public Object transform(Object input) {

            final Map.Entry<String, SkinInfo> entry = (Map.Entry<String, SkinInfo>) input;

            return new SkinBean(entry.getKey(), entry.getValue().getClassName());
        }

        /**
         * {@inheritDoc}
         */
        public void propertyChange(PropertyChangeEvent evt) {

            final SkinBean skin = (SkinBean) evt.getNewValue();

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    SubstanceLookAndFeel.setSkin(skin.getClassName());
                }
            });

            // Get back the focus
            SubstanceSkinChooserComboBox.this.getControl().requestFocusInWindow();
        }

        /**
         * Gets the display name.
         * 
         * @return the display name.
         */
        public String getDisplayName() {

            return this.displayName;
        }

        /**
         * Gets the className.
         * 
         * @return the className.
         */
        public String getClassName() {

            return this.className;
        }

        /**
         * Gets the skinBean.
         * 
         * @return the skinBean.
         */
        public SkinBean getSkinBean() {

            return skinBean;
        }

        /**
         * Sets the skinBean.
         * 
         * @param skinBean
         *            the skinBean to set.
         */
        public void setSkinBean(SkinBean skinBean) {

            Assert.notNull(skinBean, "skinBean");

            this.skinBean = skinBean;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object object) {

            if (!(object instanceof SkinBean)) {
                return Boolean.FALSE;
            }

            final SkinBean rhs = (SkinBean) object;
            return new EqualsBuilder().append(this.getClassName(), rhs.getClassName()).isEquals();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {

            final int hashCode1 = -1756175427;
            final int hashCode2 = 2141418625;

            return new HashCodeBuilder(hashCode1, hashCode2).append(this.getClassName()).toHashCode();
        }

        /**
         * Initializes the bean state.
         * 
         * @param displayName
         *            the skin display name.
         * @param className
         *            the skin class name.
         */
        private void init(String displayName, String className) {

            this.setDisplayName(displayName);
            this.setClassName(className);
        }

        /**
         * Sets the display name.
         * 
         * @param displayName
         *            the display name to set.
         */
        private void setDisplayName(String displayName) {

            Assert.notNull(displayName, "displayName");

            this.displayName = displayName;
        }

        /**
         * Sets the className.
         * 
         * @param className
         *            the className to set
         */
        private void setClassName(String className) {

            Assert.notNull(className, "className");

            this.className = className;
        }
    }
}
