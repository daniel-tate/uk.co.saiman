package uk.co.saiman.chemistry.isotope;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import uk.co.saiman.chemistry.ChemicalComposition;
import uk.co.saiman.chemistry.Element;
import uk.co.strangeskies.mathematics.Range;

public class IsotopeDistribution {
	// mass / probability
	private TreeSet<MassAbundance> data;
	// common temp object
	private HashSet<MassAbundance> dataHash;
	// molecule
	private ChemicalComposition molecule;
	// average mass
	private double mass;
	// low precision
	private boolean lowPrecision;
	// effective resolution
	private double mergeDistance = 0.1;
	// mass spectrum
	private IdealMassSpectrum massSpectrum;
	// relative abundance of whole distribution
	private double relativeAbundance = 1;

	// most significant mass
	private MassAbundance mostAbundantMass;

	// event variables
	private final EventListenerList listenerList;
	private int actionId;

	// progress tracking
	private double percentDone;

	public IsotopeDistribution() {
		data = new TreeSet<MassAbundance>();
		listenerList = new EventListenerList();
		molecule = new ChemicalComposition();
	}

	public IsotopeDistribution(IsotopeDistribution isotopeDistribution) {
		listenerList = new EventListenerList();
		if (isotopeDistribution.molecule != null) {
			molecule = isotopeDistribution.molecule;
		} else {
			molecule = null;
		}
		mass = isotopeDistribution.mass;
		lowPrecision = isotopeDistribution.lowPrecision;
		if (isotopeDistribution.data != null) {
			data = new TreeSet<MassAbundance>();

			Iterator<MassAbundance> dataIterator = isotopeDistribution.data.iterator();
			MassAbundance massAbundance;
			MassAbundance newMassAbundance;
			while (dataIterator.hasNext()) {
				massAbundance = dataIterator.next();
				newMassAbundance = new MassAbundance(massAbundance);
				data.add(newMassAbundance);
				if (massAbundance == isotopeDistribution.mostAbundantMass) {
					mostAbundantMass = newMassAbundance;
				}
			}
		} else {
			data = null;
		}
		mergeDistance = isotopeDistribution.mergeDistance;
		relativeAbundance = isotopeDistribution.relativeAbundance;
	}

	/**
	 * create isotope distribution using mass numbers, with no limit on
	 * distribution size (number of masses in output)
	 *
	 * @param molecule
	 *          molecule to calculate for
	 */
	public void calculateForMolecule(ChemicalComposition molecule) {
		this.molecule = molecule;
		calculateDistribution(0, -1, 0);
	}

	/**
	 * create isotope distribution using mass numbers, with no limit on
	 * distribution size (number of masses in output)
	 *
	 * @param molecule
	 *          molecule to calculate for
	 */
	public void calculateForMolecule(ChemicalComposition molecule, double minimumAbundance) {
		this.molecule = molecule;
		calculateDistribution(0, -1, minimumAbundance);
	}

	/**
	 * create isotope distribution using mass numbers, with no limit on
	 * distribution size (number of masses in output)
	 *
	 * @param molecule
	 *          molecule to calculate for
	 * @param lowPrecision
	 *          use only mass numbers?
	 */
	public void calculateForMolecule(ChemicalComposition molecule, boolean lowPrecision, double minimumAbundance) {
		this.molecule = molecule;
		if (lowPrecision) {
			calculateDistribution(0, -1, minimumAbundance);
		} else {
			calculateDistribution(0, 0, minimumAbundance);
		}
	}

	/**
	 * create isotope distribution using mass numbers, with a limited distribution
	 * size (number of masses in output)
	 *
	 * @param molecule
	 *          molecule to calculate for
	 * @param maxMasses
	 *          maximum number of masses to report (highest abundances only)
	 */
	public void calculateForMolecule(ChemicalComposition molecule, int maxMasses, double minimumAbundance) {
		this.molecule = molecule;
		calculateDistribution(maxMasses, -1, minimumAbundance);
	}

	/**
	 * create isotope distribution using mass numbers, with a limited distribution
	 * size (number of masses in output)
	 *
	 * @param molecule
	 *          molecule to calculate for
	 * @param maxMasses
	 *          maximum number of masses to report (highest abundances only)
	 */
	public void calculateForMolecule(ChemicalComposition molecule, int maxMasses, boolean lowPrecision,
			double minimumAbundance) {
		this.molecule = molecule;
		if (lowPrecision) {
			calculateDistribution(maxMasses, -1, minimumAbundance);
		} else {
			calculateDistribution(maxMasses, 0, minimumAbundance);
		}
	}

	/**
	 * create isotope distribution using actual mass, with no limit of
	 * distribution size (number of masses in output)
	 *
	 * @param molecule
	 *          molecule to calculate for
	 * @param resolution
	 *          maximum resolvable distance between output masses (merge closer)
	 */
	public void calculateForMolecule(ChemicalComposition molecule, double mergeDistance, double minimumAbundance) {
		this.molecule = molecule;
		if (mergeDistance < 0) {
			mergeDistance = 0;
		}
		calculateDistribution(0, mergeDistance, minimumAbundance);
	}

	/**
	 * create isotope distribution using actual mass, with a limited distribution
	 * size (number of masses in output)
	 *
	 * @param molecule
	 *          molecule to calculate for
	 * @param maxMasses
	 *          maximum number of masses to report (highest abundances only)
	 * @param resolution
	 *          maximum resolvable distance between output masses (merge closer),
	 *          with no limit of distribution size (number of masses in output)
	 */
	public void calculateForMolecule(ChemicalComposition molecule, int maxMasses, double mergeDistance,
			double minimumAbundance) {
		this.molecule = molecule;
		if (mergeDistance < 0) {
			mergeDistance = 0;
		}
		calculateDistribution(maxMasses, mergeDistance, minimumAbundance);
	}

	/**
	 * load an input stream into a new isotope distribution object
	 *
	 * @param inputReader
	 *          the stream to be read
	 */
	public void loadFromFile(File file) {
		dataHash = new HashSet<MassAbundance>();
		molecule = null;
		lowPrecision = false;

		String[] splitName = file.getName().split("\\.");
		String extension = splitName[splitName.length - 1].toLowerCase();

		try {
			if ("mzml".equals(extension)) {

			} else if ("mzxml".equals(extension)) {

			} else if ("mzdata".equals(extension)) {

			} else if ("dat".equals(extension) || "txt".equals(extension)) {
				loadPlainText(file);
			} else {
				loadPlainText(file);
			}
		} catch (IOException exception) {
			new IsotopeDistributionException(exception);
		}

		data = new TreeSet<MassAbundance>(dataHash);

		dataChangedInternal(true);
	}

	private void loadPlainText(File file) throws IOException {
		mergeDistance = 0.1;

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String text;
		String[] textSplit;
		Double mass;
		Double abundance;

		while ((text = reader.readLine()) != null) {
			text = text.replaceAll("\\,", "");
			textSplit = text.split("\\s+");
			mass = null;
			abundance = null;
			for (int i = 0; i < textSplit.length; i++) {
				try {
					if (mass == null) {
						mass = Double.parseDouble(textSplit[i]);
					} else {
						abundance = Double.parseDouble(textSplit[i]);
						i = textSplit.length;
					}
				} catch (Exception e) {}
			}
			if (mass != null) {
				if (abundance != null) {
					dataHash.add(new MassAbundance(mass, abundance));
				} else {
					mergeDistance = mass;
					if (mergeDistance <= 0) {
						mergeDistance = 0.1;
					}
				}
			}
		}

		reader.close();
	}

	/**
	 * retrieve data as a map from mass to relative abundance
	 *
	 * @return the map
	 */
	public TreeSet<MassAbundance> getData() {
		return data;
	}

	private boolean cancelled;

	public void cancelCalculation() {
		cancelled = true;
	}

	private boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Algorithm to calculate the actual distribution data. Implemented as a
	 * Markov tree (/Lattice in case of merging equivalent mass states).
	 *
	 * @param maxStates
	 *          maximum number of states to consider (most probable first). zero
	 *          for no limit.
	 * @param resolution
	 *          minimum resolvable distance between isotope combination weights.
	 *          negative to use mass numbers instead of mass.
	 */
	private void calculateDistribution(int maxStates, double mergeDistance, double minimumAbundance) {
		cancelled = false;

		lowPrecision = mergeDistance < 0;
		if (lowPrecision) {
			this.mergeDistance = 0.1;
		} else {
			if (mergeDistance == 0) {
				this.mergeDistance = 0.1;
			} else if (mergeDistance > 0) {
				this.mergeDistance = mergeDistance;
			}
		}

		// keep old data in case of interrupt
		TreeSet<MassAbundance> oldData = null;
		if (data != null) {
			oldData = new TreeSet<MassAbundance>(data);
		}

		if (molecule.getElements().isEmpty() && molecule.getIsotopes().isEmpty()) {
			data = new TreeSet<MassAbundance>();
			return;
		}

		dataHash = new HashSet<MassAbundance>();
		if (maxStates < 0) {
			maxStates = 0;
		}

		// general element data from molecule
		Map<Element, Integer> elementCounts = molecule.getElementCounts();
		// specified isotope data from molecule
		Map<Isotope, Integer> specificIsotopeCounts = molecule.getIsotopeCounts();

		/*
		 * Implement algorithm as Markov trellis, current state set defined by
		 * 'dataApproximate'.
		 */
		// check for elements with no meaningful isotope distribution (no naturally
		// occurring isotopes)
		for (Element element : elementCounts.keySet()) {
			if (element.getIsotopes().size() == 0) {
				throw new IsotopeDistributionException("No known isotopes for element: \"" + element.getName() + "\"");
			}
			if (!element.isNaturallyOccurring()) {
				String newLine = System.getProperty("line.separator");
				Iterator<Isotope> isotopeIterator = element.getIsotopes().iterator();
			}
		}

		// iterate over values in current state
		Iterator<MassAbundance> dataIterator;
		MassAbundance dataMassAbundance;
		// working variables for next state (sorted so pruning is quick as possible)
		TreeSet<MassAbundance> nextState = new TreeSet<MassAbundance>();
		MassAbundance nextMassAbundance;

		// initial state
		dataHash.add(new MassAbundance(0.0, 1.0));

		// initial progress
		long iteration = 0;
		long iterations = 0;
		percentDone = 0;
		double lastPercentDone = 0; // so we only report progress update every
																// percent

		// while{for{ to iterate through trellis progression for each element
		for (Element element : elementCounts.keySet()) {
			for (int i = 0; i < elementCounts.get(element); i++) {
				/*
				 * For progress monitoring - how long this is probably going to take.
				 */

				if (maxStates > 0) {
					long states = dataHash.size();

					long furtherIterations = 0;
					Iterator<Element> progressElementIterator = elementCounts.keySet().iterator();
					Element nextElement;
					boolean gotToHere = false; // some faffing around because we can't
																			// clone
																			// iterators
					int startFrom = 0;
					while (progressElementIterator.hasNext()) {
						nextElement = progressElementIterator.next();
						if (element == nextElement) {
							gotToHere = true;
							startFrom = i;
						}
						if (gotToHere) {
							for (int s = startFrom; s < elementCounts.get(nextElement); s++) {
								if (nextElement.isNaturallyOccurring()) {
									states *= nextElement.getNaturallyOccuringIsotopes().size();
								} else {
									states *= nextElement.getIsotopes().size();
								}

								furtherIterations += states;
								if (states > maxStates) {
									states = maxStates;
								}
							}
							startFrom = 0;
						}
					}

					for (Isotope isotope : specificIsotopeCounts.keySet()) {
						for (int s = 0; s < specificIsotopeCounts.get(isotope); s++) {
							iterations += states;
						}
					}

					iterations = iteration + furtherIterations;

					percentDone = 100d * iteration / iterations;
					if (percentDone != lastPercentDone) {
						lastPercentDone = percentDone;
						fireActionEvent("progress");
					}
				} else {
					percentDone = 50; // TODO calculate percent done in the case of
														// unbounded state set size
				}

				/*
				 * start actual calculations
				 */

				nextState.clear();

				TreeSet<Isotope> isotopes;
				if (element.isNaturallyOccurring()) {
					isotopes = element.getNaturallyOccuringIsotopes();
				} else {
					isotopes = element.getIsotopes();
				}

				for (Isotope isotope : isotopes) {
					dataIterator = dataHash.iterator();
					while (dataIterator.hasNext()) {
						dataMassAbundance = dataIterator.next();

						double mass;
						if (mergeDistance >= 0) {
							mass = dataMassAbundance.getMass() + isotope.getMass();
						} else {
							mass = dataMassAbundance.getMass() + isotope.getMassNumber();
						}
						double abundance;
						if (element.isNaturallyOccurring()) {
							abundance = dataMassAbundance.getAbundance() * isotope.getAbundance();
						} else {
							abundance = dataMassAbundance.getAbundance() / element.getIsotopes().size();
						}
						nextMassAbundance = new MassAbundance(mass, abundance);

						// if too small to be resolvable to a double then don't bother...
						if (nextMassAbundance.getAbundance() > 0) {
							// add data to next state;
							if (nextState.contains(nextMassAbundance)) {
								MassAbundance existing = nextState.floor(nextMassAbundance);
								double newAbundance = existing.getAbundance() + nextMassAbundance.getAbundance();
								double newAbundanceVariance = existing.getAbundanceVariance()
										+ nextMassAbundance.getAbundanceVariance();
								double newMassVariance = (existing.getMassVariance() * existing.getAbundance()
										+ nextMassAbundance.getMassVariance() * nextMassAbundance.getAbundance()) / newAbundance;

								nextState.remove(existing);
								nextState
										.add(new MassAbundance(existing.getMass(), newAbundance, newMassVariance, newAbundanceVariance));
							} else {
								nextState.add(nextMassAbundance);
							}
						}

						iteration++;

						if (isCancelled()) {
							data = oldData;
							return;
						}
					}
				}

				/*
				 * merge masses closer than maximum resolvable resolution
				 */
				mergeMassesWithinRange(mergeDistance, nextState);

				/*
				 * next state set of Markov chain trellis and pruning and normalising
				 * (to reduce error)
				 */
				dataHash.clear();
				dataIterator = nextState.iterator();
				double highestProbability = 0;
				if (maxStates == 0) {
					dataIterator = nextState.iterator();
					while (dataIterator.hasNext()) {
						dataMassAbundance = dataIterator.next();
						dataHash.add(dataMassAbundance);

						if (dataMassAbundance.getAbundance() > highestProbability) {
							highestProbability = dataMassAbundance.getAbundance();
						}
					}
				} else {
					TreeSet<MassAbundance> abundanceSorted = new TreeSet<MassAbundance>(new MassAbundance.AbundanceComparator());
					abundanceSorted.addAll(nextState);
					dataIterator = abundanceSorted.iterator();
					int index = 0;
					while (dataIterator.hasNext() && index++ < maxStates) {
						dataMassAbundance = dataIterator.next();
						dataHash.add(dataMassAbundance);

						if (dataMassAbundance.getAbundance() > highestProbability) {
							highestProbability = dataMassAbundance.getAbundance();
						}
					}
				}

				dataIterator = dataHash.iterator();
				while (dataIterator.hasNext()) {
					dataMassAbundance = dataIterator.next();
					dataMassAbundance.setAbundance(dataMassAbundance.getAbundance() / highestProbability);
				}

				nextState.clear();
			}
		}

		// while{for{ to iterate through trellis progression for specified isotopes
		for (Isotope specificIsotope : specificIsotopeCounts.keySet()) {
			for (int i = 0; i < specificIsotopeCounts.get(specificIsotope); i++) {
				nextState.clear();

				dataIterator = dataHash.iterator();
				while (dataIterator.hasNext()) {
					dataMassAbundance = dataIterator.next();

					nextMassAbundance = new MassAbundance();

					// are we using real mass?
					if (mergeDistance >= 0) {
						nextMassAbundance.setMass(dataMassAbundance.getMass() + specificIsotope.getMass());
					} else {
						nextMassAbundance.setMass(dataMassAbundance.getMass() + specificIsotope.getMassNumber());
					}
					nextMassAbundance.setAbundance(dataMassAbundance.getAbundance());
					// add data to next state;
					if (nextState.contains(nextMassAbundance)) {
						MassAbundance existing = nextState.floor(nextMassAbundance);
						existing.setAbundance(
								existing.getAbundance() + nextMassAbundance.getAbundance() + dataMassAbundance.getAbundance());
					} else {
						nextState.add(nextMassAbundance);
					}

					iteration++;
					if (isCancelled()) {
						data = oldData;
						return;
					}
				}

				/*
				 * don't need to merge masses closer than maximum resolvable resolution
				 * here, specific isotope shifts all states by same amount
				 */

				/*
				 * next state set of Markov chain trellis. no pruning necessary as more
				 * states not added
				 */

				dataHash.clear();
				dataHash.addAll(nextState);
				nextState.clear();
			}
		}

		/*
		 * remove all peaks below a certain value
		 */

		dataIterator = dataHash.iterator();
		while (dataIterator.hasNext()) {
			dataMassAbundance = dataIterator.next();
			if (dataMassAbundance.getAbundance() < (minimumAbundance / 100)) {
				dataIterator.remove();
			}
		}

		// make data map sorted
		data = new TreeSet<MassAbundance>();
		data.addAll(dataHash);

		// notify of change
		dataChangedInternal(true);
	}

	public void mergeMassesWithinRange(double mergeDistance) {
		mergeMassesWithinRange(mergeDistance, true);
	}

	public void mergeMassesWithinRange(double mergeDistance, boolean normalise) {
		mergeMassesWithinRange(mergeDistance, data);
		dataChanged(normalise);
	}

	protected void mergeMassesWithinRange(double mergeDistance, TreeSet<MassAbundance> data) {
		if (mergeDistance > 0) {
			boolean done;
			// keep track of previously checked state to compare
			MassAbundance lastStateMassAbundance;

			// working variables
			double newMass = 0;
			double newAbundance = 0;
			double newMassVariance = 0;
			double newAbundanceVariance = 0;
			double lastMassDifference = 0;
			double massDifference = 0;

			Iterator<MassAbundance> dataIterator;
			MassAbundance dataMassAbundance;
			do { // TODO when have more time, optimise by only reiterating over
						// already merged masses. Pretty simple.
				done = true;

				dataIterator = data.iterator();
				lastStateMassAbundance = dataIterator.next();
				// check each distance one at a time, repeat until done
				while (dataIterator.hasNext()) {
					dataMassAbundance = dataIterator.next();

					// if distance to small to be resolved then remove
					if (dataMassAbundance.getMass() - lastStateMassAbundance.getMass() < mergeDistance) {
						// merged means not done...
						done = false;

						// get new values
						// new abundance
						newAbundance = lastStateMassAbundance.getAbundance() + dataMassAbundance.getAbundance();

						// new mass
						newMass = (lastStateMassAbundance.getMass() * lastStateMassAbundance.getAbundance()
								+ dataMassAbundance.getMass() * dataMassAbundance.getAbundance()) / newAbundance;

						// new mass variance estimate!
						lastMassDifference = newMass - lastStateMassAbundance.getMass();
						massDifference = dataMassAbundance.getMass() - newMass;

						double lastMassVariance = lastStateMassAbundance.getMassVariance()
								+ (lastMassDifference * lastMassDifference);

						double massVariance = dataMassAbundance.getMassVariance() + (massDifference * massDifference);

						newMassVariance = (lastMassVariance * lastStateMassAbundance.getAbundance()
								+ massVariance * dataMassAbundance.getAbundance()) / newAbundance;

						// new abundance variance
						newAbundanceVariance = lastStateMassAbundance.getAbundanceVariance()
								+ dataMassAbundance.getAbundanceVariance();

						// apply new values
						lastStateMassAbundance.setMass(newMass);
						lastStateMassAbundance.setAbundance(newAbundance);
						lastStateMassAbundance.setMassVariance(newMassVariance);
						lastStateMassAbundance.setAbundanceVariance(newAbundanceVariance);

						// remove merged
						dataIterator.remove();
						if (dataIterator.hasNext()) {
							lastStateMassAbundance = dataIterator.next();
						}
					} else {
						lastStateMassAbundance = dataMassAbundance;
					}
				}
			} while (!done);
		}
	}

	public double getPercentDone() {
		if (percentDone < 0)
			return 0;
		if (percentDone > 100)
			return 100;
		return percentDone;
	}

	private void dataChangedInternal(boolean normalise) {
		// determine highest abundance/probability
		double totalProbability = 0;
		mostAbundantMass = new MassAbundance(0, 0);
		for (MassAbundance massAbundance : data) {
			if (massAbundance.getAbundance() > mostAbundantMass.getAbundance()) {
				mostAbundantMass = massAbundance;
			}
			totalProbability += massAbundance.getAbundance();
		}

		// normalise abundances/probabilities and get average mass
		mass = 0;
		for (MassAbundance massAbundance : data) {
			mass += massAbundance.getMass() * massAbundance.getAbundance();
			if (normalise && massAbundance != mostAbundantMass) {
				massAbundance.setAbundance(massAbundance.getAbundance() / mostAbundantMass.getAbundance());
			}
		}

		if (normalise) {
			mostAbundantMass = mostAbundantMass.withAbundance(1);
		}

		mass /= totalProbability;
	}

	public double getTotalAbundance() {
		double total = 0;
		for (MassAbundance massAbundance : data) {
			total += massAbundance.getAbundance();
		}
		return total;
	}

	public MassAbundance getLargestAbundance() {
		return mostAbundantMass;
	}

	public void dataChanged() {
		dataChanged(true);
	}

	public void dataChanged(boolean normalise) {
		dataChangedInternal(normalise);

		// invalidate related data
		massSpectrum = null;
		molecule = null;
	}

	public ChemicalComposition getMolecule() {
		return molecule;
	}

	public IdealMassSpectrum getMassSpectrum() {
		if (massSpectrum == null) {
			massSpectrum = new IdealMassSpectrum();
			// effective resolution
			massSpectrum.setEffectiveResolution(getMass() / getMergeDistance());
			massSpectrum.calculateForIsotopeDistribution(this);
		}
		return massSpectrum;
	}

	public double getMass() {
		return mass;
	}

	public Range<Double> getMassRange() {
		if (data.isEmpty()) {
			return Range.between(0d, 0d);
		}
		return Range.between(data.first().getMass(), data.last().getMass());
	}

	public void setRelativeAbundance(double relativeAbundance) {
		this.relativeAbundance = relativeAbundance;
	}

	public double getRelativeAbundance() {
		return relativeAbundance;
	}

	public SortedSet<MassAbundance> dataFromRange(Range<Double> range) {
		return data.tailSet(new MassAbundance(range.getFrom(), 0), range.isFromInclusive())
				.headSet(new MassAbundance(range.getTo(), 0), range.isToInclusive());
	}

	public IsotopeDistribution extractedFromRange(Range<Double> range) {
		IsotopeDistribution isotopeDistribution = new IsotopeDistribution(this);
		isotopeDistribution.clipToRange(range);

		return isotopeDistribution;
	}

	public boolean mergeDataFrom(IsotopeDistribution isotopeDistribution) {
		if (isotopeDistribution == null) {
			return false;
		}

		if (data == null) {
			data = new TreeSet<MassAbundance>(isotopeDistribution.getData());
			relativeAbundance = isotopeDistribution.relativeAbundance;
		} else {
			double relativeAbundanceRatio = relativeAbundance / isotopeDistribution.relativeAbundance;

			Iterator<MassAbundance> massAbundanceIterator = isotopeDistribution.getData().iterator();
			MassAbundance massAbundance;
			while (massAbundanceIterator.hasNext()) {
				massAbundance = massAbundanceIterator.next();
				massAbundance = massAbundance.withAbundance(massAbundance.getAbundance() * relativeAbundanceRatio);
				massAbundance = massAbundance
						.withAbundanceVariance(massAbundance.getAbundanceVariance() * relativeAbundanceRatio);

				if (data.floor(massAbundance).getMass() == massAbundance.getMass()) {
					MassAbundance existing = data.floor(massAbundance);
					double newAbundance = existing.getAbundance() + massAbundance.getAbundance();
					double newAbundanceVariance = existing.getAbundanceVariance() + massAbundance.getAbundanceVariance();
					double newMassVariance = (existing.getMassVariance() * existing.getAbundance()
							+ massAbundance.getMassVariance() * massAbundance.getAbundance()) / newAbundance;

					massAbundance = massAbundance.withAbundance(newAbundance);
					massAbundance = massAbundance.withAbundanceVariance(newAbundanceVariance);
					massAbundance = massAbundance.withMassVariance(newMassVariance);

					data.remove(existing);
				}

				data.add(massAbundance);
			}

			dataChanged();
		}

		return true;
	}

	public void clipToRange(Range<Double> range) {
		data = new TreeSet<MassAbundance>(dataFromRange(range));

		setRelativeAbundance(getLargestAbundance().getAbundance() * relativeAbundance);

		dataChanged(false);
	}

	public double getMergeDistance() {
		return mergeDistance;
	}

	public boolean isLowPrecision() {
		return lowPrecision;
	}

	public void removeMassAbundance(int index) {
		MassAbundance currentMassAbundance = (MassAbundance) data.toArray()[index];
		data.remove(currentMassAbundance);
		dataChanged();
	}

	public void removeMassAbundance(MassAbundance massAbundance) {
		data.remove(massAbundance);
		dataChanged();
	}

	public void removeMassAbundances(int[] indices) {
		Set<MassAbundance> massAbundancesForRemoval = new HashSet<MassAbundance>();
		for (int index : indices) {
			massAbundancesForRemoval.add((MassAbundance) data.toArray()[index]);
		}
		data.removeAll(massAbundancesForRemoval);
		dataChanged();
	}

	public void removeMassAbundances(Collection<MassAbundance> massAbundances) {
		data.removeAll(massAbundances);
		dataChanged();
	}

	public MassAbundance addMassAbundance(MassAbundance massAbundance) {
		if (data.add(massAbundance)) {
			dataChanged();
			return massAbundance;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		String string = "";
		String newLine = System.getProperty("line.separator");

		Iterator<MassAbundance> dataIterator;
		dataIterator = data.iterator();
		MassAbundance massAbundance;
		while (dataIterator.hasNext()) {
			massAbundance = dataIterator.next();

			string += massAbundance.getMass() + ": " + massAbundance.getAbundance() + newLine;
		}

		return string;
	}

	public void addActionListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	protected void fireActionEvent(String command) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				((ActionListener) listeners[i + 1]).actionPerformed(new ActionEvent(this, actionId++, command));
			}
		}
	}

	/**
	 * Filters the distribution to remove peaks which are unlikely to an effect on
	 * how the distribution is rendered at a given resolution, i.e. to save
	 * rendering time by omitting clusters of smaller peaks which are very close
	 * to larger peaks
	 *
	 * @param visibleResolution
	 */
	public void filterToResolution(double visibleResolution) {
		visibleResolution /= 2;

		Vector<MassAbundance> forRemoval = new Vector<MassAbundance>();

		long lastSampleBlock = -1;
		MassAbundance lastMassAbundance = null;
		for (MassAbundance massAbundance : data) {
			long sampleBlock = (long) (massAbundance.getMass() / visibleResolution);

			if (sampleBlock == lastSampleBlock) {
				if (lastMassAbundance.getAbundance() > massAbundance.getAbundance()) {
					forRemoval.add(massAbundance);
				} else {
					forRemoval.add(lastMassAbundance);
					lastMassAbundance = massAbundance;
				}
			} else {
				lastMassAbundance = massAbundance;
				lastSampleBlock = sampleBlock;
			}
		}

		data.removeAll(forRemoval);
	}

	public IsotopeDistribution filteredToResolution(double resolution) {
		IsotopeDistribution filteredDistribution = new IsotopeDistribution(this);
		filteredDistribution.filterToResolution(resolution);

		return filteredDistribution;
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}
}
