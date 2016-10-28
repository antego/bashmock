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

package com.github.antego.bashmock.psi.impl;

import com.github.antego.bashmock.parser.BashElementTypes;
import com.github.antego.bashmock.psi.BashVisitor;
import com.github.antego.bashmock.psi.api.BashBackquote;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * @author jansorg
 */
public class BashBackquoteImpl extends BashCompositeElement implements BashBackquote {
    public BashBackquoteImpl() {
        super(BashElementTypes.BACKQUOTE_COMMAND);
    }

    public String getCommandText() {
        return getCommandTextRange().substring(getText());
    }

    @Override
    public TextRange getCommandTextRange() {
        return TextRange.from(getStartOffset() + 1, getTextLength() - 2);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof BashVisitor) {
            ((BashVisitor) visitor).visitBackquoteCommand(this);
        } else {
            visitor.visitElement(this);
        }
    }
}
