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

package com.github.antego.bashmock.psi.impl.expression;

import com.github.antego.bashmock.parser.BashElementTypes;
import com.github.antego.bashmock.psi.BashVisitor;
import com.github.antego.bashmock.psi.api.expression.BashSubshellCommand;
import com.github.antego.bashmock.psi.impl.BashCompositeElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * @author jansorg
 */
public class BashSubshellCommandImpl extends BashCompositeElement implements BashSubshellCommand {
    public BashSubshellCommandImpl() {
        super(BashElementTypes.SUBSHELL_COMMAND);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof BashVisitor) {
            ((BashVisitor) visitor).visitSubshell(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public String getCommandText() {
        String text = getText();
        return text.substring(1, text.length() - 1); //getText doesn't include the $
    }
}
