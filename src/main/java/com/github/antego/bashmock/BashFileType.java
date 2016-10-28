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

package com.github.antego.bashmock;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * The file type implementation for Bash files.
 *
 * @author jansorg
 */
public class BashFileType extends LanguageFileType {
    public static final BashFileType BASH_FILE_TYPE = new BashFileType();
    public static final Language BASH_LANGUAGE = BASH_FILE_TYPE.getLanguage();

    /**
     * The default file extension of bash scripts.
     */
    public static final String SH_EXTENSION = "sh";
    static final String BASH_EXTENSION = "bash";

    protected BashFileType() {
        super(new BashLanguage());
    }

    @NotNull
    public String getName() {
        return "Bash";
    }

    @NotNull
    public String getDescription() {
        return "Bourne Again Shell";
    }

    @NotNull
    public String getDefaultExtension() {
        return SH_EXTENSION;
    }

    public Icon getIcon() {
        return null;
    }
}
