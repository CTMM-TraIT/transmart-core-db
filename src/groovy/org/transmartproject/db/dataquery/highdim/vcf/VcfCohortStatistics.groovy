package org.transmartproject.db.dataquery.highdim.vcf

import org.transmartproject.core.dataquery.highdim.vcf.GenomicVariantType
import org.transmartproject.core.dataquery.highdim.vcf.VcfCohortInfo

class VcfCohortStatistics implements VcfCohortInfo {
    protected VcfDataRow dataRow
    
    // Allele information for the alleles in this cohort
    List<String> alleles = []
    List<Integer> alleleCount = []
    int totalAlleleCount = 0
    List<GenomicVariantType> genomicVariantTypes = []

    // Cohort level properties
    String majorAllele = "."
    String minorAllele = "."
    Double minorAlleleFrequency = 0.0
    
    VcfCohortStatistics( VcfDataRow dataRow ) {
        this.dataRow = dataRow
        
        computeCohortStatistics()
    }
    
    @Override
    List<Double> getAlleleFrequency() {
        alleleCount.collect {  it / totalAlleleCount }
    }
    
    @Override
    String getReferenceAllele() {
        majorAllele
    }

    @Override
    List<String> getAlternativeAlleles() {
        alleles - referenceAllele
    }

    /**
     * Computes cohort level statistics
     */
    protected computeCohortStatistics() {
        Map<String,Integer> numAlleles = countAlleles()

        if( !numAlleles )
            return

        // Store generic allele distribution
        alleles = new ArrayList<String>(numAlleles.keySet())
        alleleCount = new ArrayList<Integer>(numAlleles.values())
        totalAlleleCount = alleleCount.sum()
        
        // Find the most frequent and second most frequent alleles
        majorAllele = numAlleles.max { it.value }?.key
        minorAllele = numAlleles.findAll { it.key != majorAllele }.max { it.value }?.key ?: "."
        if( minorAllele != "." )
            minorAlleleFrequency = numAlleles.getAt( minorAllele ) / totalAlleleCount
            
        // Determine genomic variant types, with the major allele as a reference
        genomicVariantTypes = getGenomicVariantTypes( majorAllele, alleles)
    }
    
    // Allele distribution for the current cohort
    Map<String,Integer> countAlleles( ) {
        List alleleNames = [] + dataRow.referenceAllele + dataRow.alternativeAlleles
        def alleleDistribution = [:].withDefault { 0 }
        for (row in dataRow.data) {
            if ( !row )
                continue;
            
            if( row.allele1 != null && row.allele1 != "." ) {
                def allele1 = alleleNames[row.allele1]
                alleleDistribution[allele1]++
            }
            
            if( row.allele2 != null && row.allele2 != "." ) {
                def allele2 = alleleNames[row.allele2]
                alleleDistribution[allele2]++
            }
        }
        alleleDistribution
    }
    
    List<GenomicVariantType> getGenomicVariantTypes(Collection<String> alleleCollection) {
        getGenomicVariantTypes(majorAllele, altCollection)
    }

    List<GenomicVariantType> getGenomicVariantTypes(String ref, Collection<String> alleleCollection) {
        alleleCollection.collect{
            ref == it ? null : GenomicVariantType.getGenomicVariantType(ref, it) 
        }
    }

    private List<Double> parseNumbersList(String numbersString) {
        parseCsvString(numbersString) {
            it.isNumber() ? Double.valueOf(it) : null
        }
    }

    private List parseCsvString(String string, Closure typeConverterClosure = { it }) {
        if (!string) {
            return []
        }

        string.split(/\s*,\s*/).collect {
            typeConverterClosure(it)
        }
    }
}
