package com.vaguehope.onosendai.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ScrollIndicatorTest {

	private static final int S3_PX_PER_UNIT = 2;
	private static final int S3_TWEET_LIST_HEIGHT_PX = 1280 - 124;

	@Test
	public void itIsNotTooHeighForS3ScreenWhenUsingAbsBarHeight () throws Exception {
		assertEquals(8, ScrollIndicator.barHeightPx(1, S3_PX_PER_UNIT));
		assertThat(ScrollIndicator.barHeightPx(200, 2), lessThan(S3_TWEET_LIST_HEIGHT_PX));
//		assertThat(ScrollIndicator.barHeightPx(500, 2), lessThan(S3_TWEET_LIST_HEIGHT_PX));
	}

	@Test
	public void itIsCorrectHeightsWhenUsingRelativeScaling () throws Exception {
		assertThat(ScrollIndicator.barHeightRelative(0), closeTo(0d, 0d));
		assertThat(ScrollIndicator.barHeightRelative(1), closeTo(0.00692d, 0.001d));
		assertThat(ScrollIndicator.barHeightRelative(5), closeTo(0.0346d, 0.003d));
		assertThat(ScrollIndicator.barHeightRelative(15), closeTo(0.0865d, 0.02d));
		assertThat(ScrollIndicator.barHeightRelative(100), closeTo(0.381d, 0.06d));
		assertThat(ScrollIndicator.barHeightRelative(500), closeTo(0.95d, 0.01d));
	}

}
