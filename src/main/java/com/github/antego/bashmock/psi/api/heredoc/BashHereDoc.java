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

package com.github.antego.bashmock.psi.api.heredoc;

import com.github.antego.bashmock.psi.api.BashPsiElement;

/**
 * @author jansorg
 */
public interface BashHereDoc extends BashPsiElement {
    /**
     * Returns whether this HereDoc is evaluating variables inside of te content of it.
     * Heredocs &lt;&lt;EOF DO highlight, but "EOF" does not.
     *
     * @return True if variables inside of the here document are highlighted and available.
     */
    boolean isEvaluatingVariables();

    /**
     * Returns whether the command receives leading whitespace of the heredoc or not.
     * &lt;&lt;- strips whitespace, &lt;&lt; does not strip whitespace.
     *
     * @return True if the leading whitespace is stripped before the text is passed on the command.
     */
    boolean isStrippingLeadingWhitespace();
}
