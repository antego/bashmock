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

package com.github.antego.bashmock.psi.api.vars;

import com.github.antego.bashmock.psi.api.BashPsiElement;
import com.github.antego.bashmock.psi.api.BashReference;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author jansorg
 */
public interface BashVar extends BashPsiElement, PsiNamedElement {
    /**
     * Return whether this variable refers to a bash built-in variable or not.
     *
     * @return True if this variable refers to a bash builtin variable
     */
    boolean isBuiltinVar();

    /**
     * Return whether this variable is a composed variable. A composed
     * variable is inside of curly brackets, e.g. ${A}
     *
     * @return True if this variable refers to a bash composed variable
     */
    boolean isParameterExpansion();

    /**
     * @return Returns true if this variable is a reference to a built-in parameter, e.g. $1 or $2 . This method
     *         does return true for $0.
     */
    boolean isParameterReference();

    /**
     * Returns whether this variable has an attached array element reference.
     *
     * @return True if it is an array variable reference.
     */
    boolean isArrayUse();

    /**
     * Overloaded to return a BashReference.
     *
     * @return
     */
    @NotNull
    BashReference getReference();

    String getReferenceName();

    boolean isVarDefinition();

    int getPrefixLength();
}
