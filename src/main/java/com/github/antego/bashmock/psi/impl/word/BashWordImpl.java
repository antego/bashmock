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
import com.github.antego.bashmock.parser.BashElementTypes;
import com.github.antego.bashmock.parser.eval.BashEnhancedLiteralTextEscaper;
import com.github.antego.bashmock.parser.eval.BashIdentityStringLiteralEscaper;
import com.github.antego.bashmock.psi.BashVisitor;
import com.github.antego.bashmock.psi.api.word.BashWord;
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
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class BashWordImpl extends BashBaseElement implements BashWord, PsiLanguageInjectionHost {
    private final static TokenSet nonWrappableChilds = TokenSet.create(BashElementTypes.STRING_ELEMENT, BashTokenTypes.STRING2, BashTokenTypes.WORD);
    private Boolean isWrapped;

    private boolean singleChildParent;
    private boolean singleChildParentComputed = false;

    public BashWordImpl(final ASTNode astNode) {
        super(astNode, "bash combined word");
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();

        this.isWrapped = null;
        this.singleChildParentComputed = false;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof BashVisitor) {
            ((BashVisitor) visitor).visitCombinedWord(this);
        } else {
            visitor.visitElement(this);
        }
    }

    public boolean isWrappable() {
        if (isSingleChildParent()) {
            return false;
        }

        ASTNode node = getNode();
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (nonWrappableChilds.contains(child.getElementType())) {
                return false;
            }
        }

        return true;
    }

    private boolean isSingleChildParent() {
        if (!singleChildParentComputed) {
            singleChildParent = BashPsiUtils.isSingleChildParent(this);
            singleChildParentComputed = true;
        }

        return singleChildParent;
    }

    @Override
    public boolean isWrapped() {
        if (isWrapped == null) {
            isWrapped = false;
            if (getTextLength() >= 2) {
                ASTNode firstChildNode = getNode().getFirstChildNode();
                if (firstChildNode != null && firstChildNode.getTextLength() >= 2) {
                    String text = firstChildNode.getText();

                    isWrapped = (text.startsWith("$'") || text.startsWith("'")) && text.endsWith("'");
                }
            }
        }

        return isWrapped;
    }

    @Override
    public String createEquallyWrappedString(String newContent) {
        if (!isWrapped()) {
            return newContent;
        }

        String firstText = getNode().getFirstChildNode().getText();
        if (firstText.startsWith("$'")) {
            return "$'" + newContent + "'";
        } else {
            return "'" + newContent + "'";
        }
    }

    public String getUnwrappedCharSequence() {
        return getTextContentRange().substring(getText());
    }

    public boolean isStatic() {
        return isWrapped() || BashPsiUtils.isStaticWordExpr(getFirstChild());
    }

    @NotNull
    public TextRange getTextContentRange() {
        if (!isWrapped()) {
            return TextRange.from(0, getTextLength());
        }

        ASTNode node = getNode();
        String first = node.getFirstChildNode().getText();
        String last = node.getLastChildNode().getText();

        int textLength = getTextLength();

        if (first.startsWith("$'") && last.endsWith("'")) {
            return TextRange.from(2, textLength - 3);
        }

        return TextRange.from(1, textLength - 2);
    }

    @Override
    public boolean isValidHost() {
        //only mark text wrapped in '' as valid injection containers
        return isWrapped();
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        return ElementManipulators.handleContentChange(this, text);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        if (!processor.execute(this, state)) {
            return false;
        }

        if (isSingleChildParent() && isWrapped()) {
            return true;
        }

        return BashElementSharedImpl.walkDefinitionScope(this, processor, state, lastParent, place);
    }

    @NotNull
    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        //$' prefix -> c-escape codes are interpreted before the injected document is parsed
        if (getText().startsWith("$'")) {
            return new BashEnhancedLiteralTextEscaper<BashWordImpl>(this);
        }

        //no $' prefix -> no escape handling
        return new BashIdentityStringLiteralEscaper<BashWordImpl>(this);
    }
}
