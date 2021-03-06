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
import com.github.antego.bashmock.psi.api.DocumentationAwareElement;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author jansorg
 */
public interface BashVarDef extends BashPsiElement, PsiNamedElement, PsiNameIdentifierOwner, NavigationItem, DocumentationAwareElement, BashVar {
    String getName();

    /**
     * Returns true if this variable definition defines an array variable.
     *
     * @return True if an array variable is being defined
     */
    boolean isArray();

    /**
     * @return True if this definition defines a ready-only variable
     */
    boolean isReadonly();

    /**
     * Returns true if this variable definition if only for the following statement.
     * E.g. "LD_LIBRARY_PATH=/usr oo-writer".
     * A local command doesn't change the current environment.
     *
     * @return True if this command is only local.
     */
    boolean isCommandLocal();

    /**
     * The psi element which is the identifier of the assignment.
     *
     * @return The name element
     */
    @NotNull
    PsiElement findAssignmentWord();

    /**
     * Returns whether this variable definition defines a local variable or already is defined as a local variable.
     * If a variable is declared using the "local" keyword in a function then it is a function local variable,
     * i.e. this method returns true in that case.
     *
     * @return True if it's local in a function
     */
    boolean isFunctionScopeLocal();

    /**
     * Returns whether this variable definition defines a local variable definition.
     *
     * @return
     */
    boolean isLocalVarDef();

    PsiElement findFunctionScope();

    /**
     * @return True if this is a variable definition with assignment, e.g. "export a=1"
     */
    boolean hasAssignmentValue();

    @NotNull
    BashReference getReference();

    /**
     * @return True if the value of the assignment word is static, false otherwise. Something like export "$a"=b is not a static assignment word
     */
    boolean isStaticAssignmentWord();
}
