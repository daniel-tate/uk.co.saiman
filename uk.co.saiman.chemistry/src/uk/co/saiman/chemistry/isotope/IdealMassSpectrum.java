package uk.co.saiman.chemistry.isotope;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.co.saiman.chemistry.ChemicalComposition;
import uk.co.strangeskies.mathematics.Range;

public class IdealMassSpectrum {
	// data
	private TreeMap<Double, Double> data;

	// source molecule
	private ChemicalComposition molecule;

	// effective resolution
	private double effectiveResolution;
	private static final double fwhmCoefficient = 0.5 / Math.sqrt(2 * Math.log(2));

	// relative abundance of whole spectrum
	private double relativeAbundance = 1;

	public IdealMassSpectrum() {
		// mass spectrum data
		data = new TreeMap<Double, Double>();
		this.molecule = null;
	}

	public IdealMassSpectrum(IdealMassSpectrum massSpectrum) {
		data = new TreeMap<Double, Double>();
		if (massSpectrum.molecule != null) {
			molecule = massSpectrum.molecule;
		} else {
			molecule = null;
		}
		if (massSpectrum.data != null) {
			data = new TreeMap<Double, Double>();

			Iterator<Double> dataIterator = massSpectrum.data.keySet().iterator();
			Double dataItem;
			while (dataIterator.hasNext()) {
				dataItem = dataIterator.next();
				data.put(dataItem, massSpectrum.data.get(dataItem));
			}
		} else {
			data = null;
		}
		setEffectiveResolution(massSpectrum.getEffectiveResolution());
		relativeAbundance = massSpectrum.relativeAbundance;
	}

	public void calculateForIsotopeDistribution(IsotopeDistribution isotopeDistribution) {
		calculateForIsotopeDistribution(isotopeDistribution, null, null, 0, true);
	}

	public void calculateForIsotopeDistribution(IsotopeDistribution isotopeDistribution, IdealMassSpectrum extremaSpectra,
			Range<Double> massRange, double sampleResolution, boolean normalise) {
		molecule = isotopeDistribution.getMolecule();

		double normal = isotopeDistribution.getTotalAbundance();

		// clear data
		data.clear();

		if (isotopeDistribution.isEmpty()) {
			return;
		}

		relativeAbundance = isotopeDistribution.getRelativeAbundance();

		// distribution data
		SortedSet<MassAbundance> distributionData = isotopeDistribution.getData();

		// initial pass to find largest variance to get range of spectrum and
		// calculate effective standard deviations and peak heights
		double largestVariance = 0;
		Map<MassAbundance, Double> effectiveStandardDeviations = new HashMap<MassAbundance, Double>();
		Map<MassAbundance, Double> peakHeightReciprocals = new HashMap<MassAbundance, Double>();
		Iterator<MassAbundance> distributionIterator = distributionData.iterator();
		MassAbundance massAbundance;

		while (distributionIterator.hasNext()) {
			massAbundance = distributionIterator.next();

			double localPeakWidth = massAbundance.getMass() / getEffectiveResolution() * fwhmCoefficient;

			// effectively convolve our gaussian filter with the variance of any
			// merged mass points
			effectiveStandardDeviations.put(massAbundance,
					Math.sqrt(massAbundance.getMassVariance() + localPeakWidth * localPeakWidth));
			peakHeightReciprocals.put(massAbundance, effectiveStandardDeviations.get(massAbundance) * Math.sqrt(Math.PI * 2));

			// find largest variance
			if (massAbundance.getMassVariance() > largestVariance) {
				largestVariance = massAbundance.getMassVariance();
			}
		}

		double minimumPeakWidth = distributionData.first().getMass() / getEffectiveResolution();

		// padding of total sample space range on either side of distribution
		double padding = Math.sqrt(largestVariance) + minimumPeakWidth;
		padding *= 4;

		// sample step size
		if (sampleResolution == 0) {
			sampleResolution = minimumPeakWidth * 0.2;
		}

		// current effective sample set
		SortedSet<MassAbundance> sampleSet = new TreeSet<MassAbundance>();
		Iterator<MassAbundance> sampleSetIterator;
		MassAbundance sampleItem = new MassAbundance(0, 0);

		// initial sample mass
		double startMass;
		double endMass;
		if (massRange != null) {
			startMass = massRange.getFrom();
			endMass = massRange.getTo();
		} else {
			startMass = distributionData.first().getMass() - padding;
			endMass = distributionData.last().getMass() + padding;
		}

		MassAbundance sampleScanPosition = new MassAbundance(startMass - padding, 1);

		double sampleMass = startMass;
		boolean skippedPreviousSample = true;
		double previousSampleValue = 0;
		// sample at each step
		while (sampleMass <= endMass) {
			// find all masses no longer close enough to affect current sample
			sampleSetIterator = sampleSet.iterator();
			while (sampleSetIterator.hasNext()) {
				sampleItem = sampleSetIterator.next();
				if (sampleMass > sampleItem.getMass() + effectiveStandardDeviations.get(sampleItem) * 4) {
					sampleSetIterator.remove();
				}
			}

			// find all masses now close enough to affect current sample
			sampleSetIterator = distributionData.tailSet(sampleScanPosition).iterator();
			// sampleSetIterator.next();
			boolean done = false;
			while (sampleSetIterator.hasNext() && !done) {
				sampleItem = sampleSetIterator.next();
				if (sampleMass >= sampleItem.getMass() - effectiveStandardDeviations.get(sampleItem) * 4) {
					sampleSet.add(sampleItem);
				} else {
					sampleScanPosition = sampleItem;
					done = true;
				}
			}

			// working variables
			double sampleValue = 0;
			double powerNumerator;
			double powerDenominator;
			double sampleFromItem;
			double difference;
			// go through each item affecting this sample
			sampleSetIterator = sampleSet.iterator();
			while (sampleSetIterator.hasNext()) {
				sampleItem = sampleSetIterator.next();

				powerNumerator = sampleMass - sampleItem.getMass();
				powerNumerator *= -powerNumerator;

				powerDenominator = effectiveStandardDeviations.get(sampleItem);
				powerDenominator *= powerDenominator * 2;

				sampleFromItem = Math.pow(Math.E, powerNumerator / powerDenominator) / peakHeightReciprocals.get(sampleItem)
						* sampleItem.getAbundance();

				// if near edges then interpolate to 0 for smoother fall-off.
				difference = Math.abs(sampleMass - sampleItem.getMass());
				if (difference > effectiveStandardDeviations.get(sampleItem) * 3) {
					sampleFromItem *= 1 - (difference - effectiveStandardDeviations.get(sampleItem) * 3)
							/ effectiveStandardDeviations.get(sampleItem);
				}

				sampleValue += sampleFromItem;
			}

			sampleValue /= normal;

			Range<Double> extrema = null;
			if (extremaSpectra != null) {
				extrema = extremaSpectra.getAbundanceExtremaInRange(Range.between(sampleMass - sampleResolution, sampleMass));
			}

			if ((extrema != null || sampleValue > 0) && skippedPreviousSample) {
				skippedPreviousSample = false;
				data.put(sampleMass - sampleResolution, 0d);
			}

			if (extrema != null) {
				if (extrema.getFrom() < sampleValue && extrema.getFrom() < previousSampleValue) {
					data.put(sampleMass - sampleResolution * 0.75, extrema.getFrom());
				}

				if (extrema.getTo() > sampleValue && extrema.getTo() > previousSampleValue) {
					data.put(sampleMass - sampleResolution * 0.25, extrema.getTo());
				}
			}

			if (sampleValue > 0) {
				data.put(sampleMass, sampleValue);
			} else if (!skippedPreviousSample) {
				data.put(sampleMass, 0d);
				skippedPreviousSample = true;
			}

			previousSampleValue = sampleValue;

			sampleMass += sampleResolution;
		}
		if (!skippedPreviousSample) {
			data.put(sampleMass, 0d);
		}

		if (normalise && !data.isEmpty()) {
			/*
			 * here we merge the distribution by half the peak width and then find the
			 * [n] most abundant masses, which should translate to about the location
			 * of the [n] tallest peaks in the mass spectrum. We then sample these
			 * points and take the largest to normalise by, by creating a mass
			 * spectrum with a single sample at those points. Not perfect, but pretty
			 * close in practice.
			 */
			IsotopeDistribution significantMassDistribution = new IsotopeDistribution(isotopeDistribution);
			significantMassDistribution.mergeMassesWithinRange(getMass() / getEffectiveResolution() * 0.25, false);

			IsotopeDistribution workingDistribution = new IsotopeDistribution(significantMassDistribution);

			double largestAbundance = 0;
			for (int i = 0; i < 5; i++) {
				MassAbundance mostAbundantMass = workingDistribution.getLargestAbundance();
				workingDistribution.removeMassAbundance(mostAbundantMass);

				IdealMassSpectrum significantMassSpectrum = new IdealMassSpectrum();
				significantMassSpectrum.setEffectiveResolution(getEffectiveResolution());
				significantMassSpectrum.calculateForIsotopeDistribution(significantMassDistribution, null,
						Range.between(mostAbundantMass.getMass(), mostAbundantMass.getMass()).setInclusive(false, false), 1, false);

				double abundance = significantMassSpectrum.getLargestAbundance();
				if (abundance > largestAbundance) {
					largestAbundance = abundance;
				}
			}

			normalise(largestAbundance);
		}
	}

	/**
	 * Find the range of abundances present within this range for this spectra.
	 *
	 * @param massRange
	 *          the range between which to check for extrema.
	 * @return the range between the maxima and minima as approximated by any
	 *         samples within the range given, null if there are no such samples.
	 */
	protected Range<Double> getAbundanceExtremaInRange(Range<Double> massRange) {
		Map<Double, Double> samplesWithinRange = data.tailMap(massRange.getFrom()).headMap(massRange.getTo());

		if (samplesWithinRange.isEmpty()) {
			return null;
		}

		Iterator<Double> abundanceIterator = samplesWithinRange.values().iterator();
		Double abundance = abundanceIterator.next();
		Range<Double> abundanceRange = Range.between(abundance, abundance);
		while (abundanceIterator.hasNext()) {
			abundanceRange.getExtendedThrough(abundanceIterator.next(), false);
		}

		return abundanceRange;
	}

	public void normalise(double normalAbundance) {
		// normalise results
		Iterator<Double> dataIterator;
		Double dataSample;

		dataIterator = data.keySet().iterator();
		while (dataIterator.hasNext()) {
			dataSample = dataIterator.next();
			data.put(dataSample, data.get(dataSample) / normalAbundance);
		}
	}

	public void normalise() {
		// normalise results
		Iterator<Double> dataIterator;
		Double dataSample;

		dataIterator = data.keySet().iterator();
		while (dataIterator.hasNext()) {
			dataSample = dataIterator.next();
			data.put(dataSample, data.get(dataSample) / getLargestAbundance());
		}
	}

	public double getLargestAbundance() {
		Iterator<Double> dataIterator;
		Double dataSample;
		Double largestAbundance = 0d;

		dataIterator = data.keySet().iterator();
		while (dataIterator.hasNext()) {
			dataSample = dataIterator.next();
			if (data.get(dataSample) > largestAbundance) {
				largestAbundance = data.get(dataSample);
			}
		}

		return largestAbundance;
	}

	public TreeMap<Double, Double> getData() {
		return data;
	}

	public void setRelativeAbundance(double relativeAbundance) {
		this.relativeAbundance = relativeAbundance;
	}

	public double getRelativeAbundance() {
		return relativeAbundance;
	}

	public void setEffectiveResolution(double effectiveResolution) {
		this.effectiveResolution = effectiveResolution;
	}

	public double getEffectiveResolution() {
		return effectiveResolution;
	}

	public ChemicalComposition getMolecule() {
		return molecule;
	}

	public Range<Double> getRange() {
		return Range.between(data.firstEntry().getKey(), data.lastEntry().getKey());
	}

	public double getInterpolatedAbundance(double mass) {
		if (!getRange().contains(mass)) {
			return 0;
		}

		Map.Entry<Double, Double> below = data.floorEntry(mass);
		Map.Entry<Double, Double> above = data.ceilingEntry(mass);

		if (below == above) {
			return below.getValue();
		}

		double interpolated = ((mass - below.getKey()) * below.getValue() + (above.getKey() - mass) * above.getValue())
				/ getMassStepSize();

		return interpolated;
	}

	public double getMassStepSize() {
		return (getRange().getTo() - getRange().getFrom()) / data.size();
	}

	public NavigableMap<Double, Double> dataFromRange(Range<Double> range) {
		return data.tailMap(range.getFrom(), false).headMap(range.getTo(), false);
	}

	public IdealMassSpectrum extractFromRange(Range<Double> range) {
		IdealMassSpectrum massSpectrum = new IdealMassSpectrum(this);
		massSpectrum.clipToRange(range);

		return massSpectrum;
	}

	public void clipToRange(Range<Double> range) {
		TreeMap<Double, Double> newData = new TreeMap<Double, Double>(dataFromRange(range));

		newData.put(range.getFrom(), getInterpolatedAbundance(range.getFrom()));
		newData.put(range.getTo(), getInterpolatedAbundance(range.getTo()));

		data = newData;

		double highestAbundance = 0;
		Iterator<Map.Entry<Double, Double>> massAbundanceIterator = data.entrySet().iterator();
		while (massAbundanceIterator.hasNext()) {
			double abundance = massAbundanceIterator.next().getValue();
			if (abundance > highestAbundance) {
				highestAbundance = abundance;
			}
		}
		setRelativeAbundance(highestAbundance * relativeAbundance);
	}

	public double getNormalisingConstant() {
		double total = 0;

		Iterator<Double> sampleIterator = data.values().iterator();
		while (sampleIterator.hasNext()) {
			total += sampleIterator.next();
		}

		return 1 / total;
	}

	public double getMass() {
		if (molecule != null) {
			return molecule.getAverageMass();
		}

		double totalMass = 0;
		double totalAbundance = 0;
		Iterator<Map.Entry<Double, Double>> massAbundanceIterator = data.entrySet().iterator();
		Map.Entry<Double, Double> massAbundance;
		while (massAbundanceIterator.hasNext()) {
			massAbundance = massAbundanceIterator.next();
			totalMass += massAbundance.getKey() * massAbundance.getValue();
			totalAbundance += massAbundance.getValue();
		}
		return totalMass / totalAbundance;
	}

	public double getBestFitScaleAgainst(IdealMassSpectrum massSpectrum) {
		Range<Double> intersectionRange = getRange().getIntersectionWith(massSpectrum.getRange());

		double weightedAverageScale = 0;
		if (!intersectionRange.isEmpty()) {
			double weight;
			double weightSum = 0;
			double mass;
			double abundance;
			double otherAbundance;
			if (getMassStepSize() < massSpectrum.getMassStepSize()) {
				Iterator<Double> intersectionIterator = dataFromRange(intersectionRange).keySet().iterator();

				while (intersectionIterator.hasNext()) {
					mass = intersectionIterator.next();
					abundance = data.get(mass);
					otherAbundance = massSpectrum.getInterpolatedAbundance(mass);
					weightSum += weight = Math.sqrt(otherAbundance * abundance);
					weightedAverageScale += (otherAbundance / abundance) * weight;
				}
			} else {
				Iterator<Double> intersectionIterator = massSpectrum.dataFromRange(intersectionRange).keySet().iterator();

				while (intersectionIterator.hasNext()) {
					mass = intersectionIterator.next();
					abundance = getInterpolatedAbundance(mass);
					otherAbundance = massSpectrum.getData().get(mass);
					weightSum += weight = Math.sqrt(otherAbundance * abundance);
					weightedAverageScale += (otherAbundance / abundance) * weight;
				}
			}
			if (weightedAverageScale == 0) {
				weightedAverageScale = 1;
			} else {
				weightedAverageScale /= weightSum;
			}
		} else {
			weightedAverageScale = 1;
		}

		return 1 / weightedAverageScale;
	}
}
