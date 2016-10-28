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

package com.github.antego.bashmock.psi.impl.vars;

import com.github.antego.bashmock.psi.api.vars.BashVarDef;
import com.github.antego.bashmock.psi.util.BashAbstractProcessor;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author jansorg
 */
public class BashVarVariantsProcessor extends BashAbstractProcessor implements BashVarCollectorProcessor {
    private final List<BashVarDef> variables = Lists.newLinkedList();
    private final Set<String> variableNames = Sets.newHashSet();
    private final PsiElement startElement;

    public BashVarVariantsProcessor(PsiElement startElement) {
        super(false);

        this.startElement = startElement;
    }

    public boolean execute(@NotNull PsiElement psiElement, @NotNull ResolveState resolveState) {
        if (psiElement instanceof BashVarDef) {
            final BashVarDef varDef = (BashVarDef) psiElement;
            if (varDef.isStaticAssignmentWord() && !varDef.isCommandLocal() && !variableNames.contains(varDef.getName()) && BashVarUtils.isInDefinedScope(startElement, varDef)) {
                variables.add(varDef);
                variableNames.add(varDef.getName());
            }
        }

        return true;
    }

    public List<BashVarDef> getVariables() {
        return variables;
    }

    public <T> T getHint(@NotNull Key<T> tKey) {
        return null;
    }
}