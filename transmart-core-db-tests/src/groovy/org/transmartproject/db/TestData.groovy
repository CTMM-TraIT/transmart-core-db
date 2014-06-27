package org.transmartproject.db

import org.transmartproject.db.dataquery.clinical.ClinicalTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.dataquery.highdim.acgh.AcghTestData
import org.transmartproject.db.dataquery.highdim.mrna.MrnaTestData
import org.transmartproject.db.i2b2data.I2b2Data
import org.transmartproject.db.ontology.ConceptTestData

class TestData {

    ConceptTestData conceptData
    I2b2Data i2b2Data
    ClinicalTestData clinicalData
    MrnaTestData mrnaData
    AcghTestData acghData
    SampleBioMarkerTestData bioMarkerTestData

    static TestData createDefault() {
        def conceptData = ConceptTestData.createDefault()
        def i2b2Data = I2b2Data.createDefault()
        def clinicalData = ClinicalTestData.createDefault(conceptData.i2b2List, i2b2Data.patients)

        def bioMarkerTestData = new SampleBioMarkerTestData()
        def mrnaData = new MrnaTestData('2', bioMarkerTestData) //concept code '2'
        def acghData = new AcghTestData('4', bioMarkerTestData) //concept code '4'

        new TestData(
                conceptData: conceptData,
                i2b2Data: i2b2Data,
                clinicalData: clinicalData,
                mrnaData:  mrnaData,
                acghData: acghData,
                bioMarkerTestData: bioMarkerTestData,
        )
    }

    void saveAll() {
        conceptData?.saveAll()
        i2b2Data?.saveAll()
        clinicalData?.saveAll()
        bioMarkerTestData?.saveAll()
        mrnaData?.saveAll()
        mrnaData?.updateDoubleScaledValues()
        acghData?.saveAll()
    }
}
