package org.apache.ibatis.executor.resultset;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author Guillaume Darmont / guillaume@dropinocean.com
 */
public class LazyListTest {

	private final int[] fakeResultList = new int[] { 16, 32, 64 };

	@Test
	public void checkBaseConstructors() {
		CursorList<Integer> cursorList = mock(CursorList.class);
		List list = cursorList;

		LazyList<Integer> lazyList = new LazyList<Integer>(cursorList);
		assertNotNull(lazyList);

		lazyList = new LazyList<Integer>(list);
		assertNotNull(lazyList);
		lazyList = null;

		try {
			list = new ArrayList();
			lazyList = new LazyList<Integer>(list);
			fail("Shouldn't be here...");
		} catch (IllegalArgumentException e) {
			assertNull(lazyList);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void checkConstructorOnAlreadyUsedCursorList() {
		CursorList<Integer> cursorList = mock(CursorList.class);
		when(cursorList.isFetchStarted()).thenReturn(Boolean.TRUE);

		LazyList<Integer> lazyList = new LazyList<Integer>(cursorList);
		fail("Shouldn't be here...");
	}

	@Test
	public void checkCursorListThenLocalStorageAccess() {
		CursorList<Integer> cursorList = mock(CursorList.class);
		when(cursorList.fetchNextObjectFromDatabase()).thenReturn(fakeResultList[0]).thenReturn(fakeResultList[1])
				.thenReturn(fakeResultList[2]).thenReturn(null);

		LazyList<Integer> lazyList = new LazyList<Integer>(cursorList);
		int index = 0;
		for (Integer i : lazyList) {
			assertEquals(Integer.valueOf(fakeResultList[index++]), i);
		}
		verify(cursorList, times(4)).fetchNextObjectFromDatabase();
		assertEquals(3, lazyList.size());

		// Mock cursorList to only return null, so we can check that internal storage is used
		reset(cursorList);
		when(cursorList.fetchNextObjectFromDatabase()).thenReturn(null);
		index = 0;
		for (Integer i : lazyList) {
			assertEquals(Integer.valueOf(fakeResultList[index++]), i);
		}

		verify(cursorList, times(1)).fetchNextObjectFromDatabase();
		assertEquals(3, lazyList.size());
	}

}
