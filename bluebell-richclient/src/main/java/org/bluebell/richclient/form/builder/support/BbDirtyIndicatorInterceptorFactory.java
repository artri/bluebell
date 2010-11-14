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

package org.bluebell.richclient.form.builder.support;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.support.ConfigurableFormComponentInterceptorFactory;

/**
 * Factory for creating {@link BbDirtyIndicatorInterceptor} instances.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDirtyIndicatorInterceptorFactory extends ConfigurableFormComponentInterceptorFactory {

    /**
     * {@inheritDoc}.
     */
    @Override
    protected FormComponentInterceptor createInterceptor(FormModel formModel) {

        return new BbDirtyIndicatorInterceptor(formModel);
    }
}
