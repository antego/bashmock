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

package com.github.antego.bashmock.psi.impl.word;

import com.github.antego.bashmock.lexer.BashTokenTypes;
import com.github.antego.bashmock.parser.eval.BashSimpleTextLiteralEscaper;
import com.github.antego.bashmock.psi.BashVisitor;
import com.github.antego.bashmock.psi.api.BashCharSequence;
import com.github.antego.bashmock.psi.api.BashString;
import com.github.antego.bashmock.psi.impl.BashBaseElement;
import com.github.antego.bashmock.psi.impl.BashElementSharedImpl;
import com.github.antego.bashmock.psi.util.BashPsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * A string spanning start and end markers and content elements.
 * <br>
 * @author jansorg
 */
public class BashStringImpl extends BashBaseElement implements BashString, BashCharSequence, PsiLanguageInjectionHost {
    private TextRange contentRange;
    private Boolean isWrapped;

    public BashStringImpl(ASTNode node) {
        super(node, "Bash string");
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();

        this.contentRange = null;
        this.isWrapped = null;
    }

    @Override
    public boolean isWrapped() {
        if (isWrapped == null) {
            isWrapped = false;

            if (getTextLength() >= 2) {
                ASTNode node = getNode();
                IElementType firstType = node.getFirstChildNode().getElementType();
                IElementType lastType = node.getLastChildNode().getElementType();

                isWrapped = firstType == BashTokenTypes.STRING_BEGIN && lastType == BashTokenTypes.STRING_END;
            }
        }

        return isWrapped;
    }

    @Override
    public String createEquallyWrappedString(String newContent) {
        ASTNode node = getNode();
        ASTNode firstChild = node.getFirstChildNode();
        ASTNode lastChild = node.getLastChildNode();

        StringBuilder result = new StringBuilder(firstChild.getTextLength() + newContent.length() + lastChild.getTextLength());
        return result.append(firstChild.getText()).append(newContent).append(lastChild.getText()).toString();
    }

    public String getUnwrappedCharSequence() {
        return getTextContentRange().substring(getText());
    }

    public boolean isStatic() {
        return getTextContentRange().getLength() == 0 || BashPsiUtils.isStaticWordExpr(getFirstChild());
    }

    @NotNull
    public TextRange getTextContentRange() {
        if (contentRange == null) {
            ASTNode node = getNode();
            ASTNode firstChild = node.getFirstChildNode();

            if (firstChild != null && firstChild.getText().equals("$\"")) {
                contentRange = TextRange.from(2, getTextLength() - 3);
            } else {
                contentRange = TextRange.from(1, getTextLength() - 2);
            }
        }

        return contentRange;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof BashVisitor) {
            ((BashVisitor) visitor).visitString(this);
        } else {
            visitor.visitElement(this);
        }
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        if (!processor.execute(this, state)) {
            return false;
        }

        boolean walkOn = isStatic() || BashElementSharedImpl.walkDefinitionScope(this, processor, state, lastParent, place);

        /*if (walkOn && isValidHost()) {
            walkOn = InjectionUtils.walkInjection(this, processor, state, lastParent, place, true);
        } */

        return walkOn;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        return ElementManipulators.handleContentChange(this, text);
    }

    @NotNull
    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new BashSimpleTextLiteralEscaper<BashStringImpl>(this);
    }
}
