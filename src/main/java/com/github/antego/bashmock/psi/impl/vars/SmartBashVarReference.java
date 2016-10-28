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

import com.github.antego.bashmock.psi.util.BashResolveUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * Variable reference implementation for smart mode.
 *
 * @author jansorg
 */
class SmartBashVarReference extends AbstractBashVarReference {

    public SmartBashVarReference(BashVarImpl bashVar) {
        super(bashVar);
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
        return BashResolveUtil.resolve(bashVar, true, false);
    }

}
