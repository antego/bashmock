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

package com.github.antego.bashmock.psi.impl.loops;

import com.github.antego.bashmock.lexer.BashTokenTypes;
import com.github.antego.bashmock.parser.BashElementTypes;
import com.github.antego.bashmock.psi.api.loops.BashSelect;
import com.github.antego.bashmock.psi.impl.BashKeywordDefaultImpl;
import com.intellij.psi.PsiElement;

/**
 * @author jansorg
 */
public class BashSelectImpl extends BashKeywordDefaultImpl implements BashSelect {
    public BashSelectImpl() {
        super(BashElementTypes.SELECT_COMMAND);
    }

    public PsiElement keywordElement() {
        return findPsiChildByType(BashTokenTypes.SELECT_KEYWORD);
    }

    @Override
    public boolean isCommandGroup() {
        return false;
    }
}
