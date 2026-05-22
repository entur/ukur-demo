/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.entur.demo.ukur;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import uk.org.siri.siri20.Siri;

public final class SiriJaxbContextHolder {

    public static final JAXBContext INSTANCE;

    static {
        try {
            INSTANCE = JAXBContext.newInstance(Siri.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not initialize JAXBContext for Siri", e);
        }
    }

    private SiriJaxbContextHolder() {}
}