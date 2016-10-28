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

import com.ansorgit.plugins.bash.file.BashFileType;
import com.github.antego.bashmock.parser.BashElementTypes;
import com.github.antego.bashmock.psi.BashVisitor;
import com.github.antego.bashmock.psi.api.BashFile;
import com.github.antego.bashmock.psi.api.BashShebang;
import com.github.antego.bashmock.psi.api.function.BashFunctionDef;
import com.github.antego.bashmock.psi.stubs.api.BashFileStub;
import com.github.antego.bashmock.psi.stubs.api.BashFunctionDefStub;
import com.github.antego.bashmock.psi.util.BashResolveUtil;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PSI implementation for a Bash file.
 */
public class BashFileImpl extends PsiFileBase implements BashFile {
    public BashFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, BashFileType.BASH_LANGUAGE);
    }

    @Nullable
    @Override
    public BashFileStub getStub() {
        return (BashFileStub) super.getStub();
    }

    @NotNull
    public FileType getFileType() {
        return BashFileType.BASH_FILE_TYPE;
    }

    public boolean hasShebangLine() {
        return findShebang() != null;
    }

    @Nullable
    @Override
    public BashShebang findShebang() {
        return findChildByClass(BashShebang.class);
    }


    public BashFunctionDef[] functionDefinitions() {
        BashFileStub stub = getStub();
        if (stub != null) {
            return stub.getChildrenByType(BashElementTypes.FUNCTION_DEF_COMMAND, BashFunctionDefStub.ARRAY_FACTORY);
        }

        return findChildrenByClass(BashFunctionDef.class);
    }

    @Override
    public boolean processDeclarations(@NotNull final PsiScopeProcessor processor, @NotNull final ResolveState state, final PsiElement lastParent, @NotNull final PsiElement place) {
        return BashResolveUtil.processContainerDeclarations(this, processor, state, lastParent, place);
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return BashElementSharedImpl.getElementUseScope(this, getProject());
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof BashVisitor) {
            ((BashVisitor) visitor).visitFile(this);
        } else {
            visitor.visitFile(this);
        }
    }
}
