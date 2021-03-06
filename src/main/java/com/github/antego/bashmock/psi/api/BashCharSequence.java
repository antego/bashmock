/*
 * Copyright (c) Joachim Ansorg, mail@ansorg-it.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.antego.bashmock.psi.api;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

/**
 * @author jansorg
 */
public interface BashCharSequence extends BashPsiElement {
    boolean isWrapped();

    String createEquallyWrappedString(String newContent);

    /**
     * Returns the char sequence without any start or end markers.
     *
     * @return The string value without marks
     */
    String getUnwrappedCharSequence();

    /**
     * Returns whether this char sequence is static or whether its value depends on runtime information.
     * A static sequence does not contain any variables, etc.
     *
     * @return True if its value is known at write-time.
     */
    boolean isStatic();

    /**
     * Returns the range of the actual text content of this element.
     *
     * @return The range inside this element around the content elements
     */
    @NotNull
    TextRange getTextContentRange();
}
