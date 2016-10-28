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

package com.github.antego.bashmock.psi.api.function;

import com.github.antego.bashmock.psi.api.BashBlock;
import com.github.antego.bashmock.psi.api.BashFunctionDefName;
import com.github.antego.bashmock.psi.api.BashPsiElement;
import com.github.antego.bashmock.psi.api.DocumentationAwareElement;
import com.github.antego.bashmock.psi.api.vars.BashVarDefContainer;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author jansorg
 */
public interface BashFunctionDef extends BashPsiElement, PsiNamedElement, NavigationItem, PsiNameIdentifierOwner, BashVarDefContainer, DocumentationAwareElement {
    /**
     * Returns the function body. A valid function definition always has a valid body.
     *
     * @return The body of the function.
     */
    @Nullable
    BashBlock functionBody();

    @Nullable
    BashFunctionDefName getNameSymbol();

    /**
     * The list of parameters used inside this function definition. Sub-functions are not searched for parameters
     * uses.
     *
     * @return The list of parameter variable uses
     */
    @NotNull
    List<BashPsiElement> findReferencedParameters();
}
