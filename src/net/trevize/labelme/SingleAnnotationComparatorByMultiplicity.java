package net.trevize.labelme;



import java.util.Comparator;

/**
 * This class is a SingleAnnotation comparator on the multiplicity.
 * 
 * @author Nicolas James <nicolas.james@gmail.com> [[http://njames.trevize.net]]
 *
 */

public class SingleAnnotationComparatorByMultiplicity implements
		Comparator<SingleAnnotation> {
	@Override
	public int compare(SingleAnnotation o1, SingleAnnotation o2) {
		int df = (Integer) (o1.multiplicity.getValue())
				- (Integer) (o2.multiplicity.getValue());
		if (df > 0) {
			return 1;
		} else if (df == 0) {
			return 0;
		} else {
			return -1;
		}
	}
}
