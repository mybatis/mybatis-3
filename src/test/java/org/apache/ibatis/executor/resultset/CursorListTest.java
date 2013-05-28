package org.apache.ibatis.executor.resultset;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author Guillaume Darmont / guillaume@dropinocean.com
 */
@RunWith(MockitoJUnitRunner.class)
public class CursorListTest {

	private final int[] fakeResultList = new int[] { 16, 32, 64 };

	@Test(expected = UnsupportedOperationException.class)
	public void testGet() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);
		CursorList cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);
		cursorList.get(0);
		fail("get() call should not be available");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSize() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);
		CursorList cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);
		cursorList.size();
		fail("size() call should not be available");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIteratorRemove() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);
		CursorList cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);

		Iterator<Integer> iter = cursorList.iterator();
		iter.remove();
		fail("iter.remove() call should not be available");
	}

	@Test
	public void testNextWithoutHasNext() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);
		CursorList cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);

		Iterator<Integer> iter = cursorList.iterator();
		assertEquals(Integer.valueOf(16), iter.next());
		assertEquals(Integer.valueOf(32), iter.next());
		assertEquals(Integer.valueOf(64), iter.next());
	}

	@Test
	public void ensureToStringDoesnotExhaustResultSet() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);
		CursorList cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);

		cursorList.toString();
		assertFalse(cursorList.isResultSetExhausted());
	}

	@Test
	public void testCursorFetch() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);

		CursorList<Integer> cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);
		assertFalse(cursorList.isFetchStarted());
		assertFalse(cursorList.isResultSetExhausted());

		Iterator<Integer> iter = cursorList.iterator();

		// Fetching 1st item
		assertTrue(iter.hasNext());
		assertEquals(Integer.valueOf(16), iter.next());
		assertTrue(cursorList.isFetchStarted());
		assertFalse(cursorList.isResultSetExhausted());

		// Fetching 2nd item
		assertTrue(iter.hasNext());
		assertEquals(Integer.valueOf(32), iter.next());

		// Fetching 3rd item
		assertTrue(iter.hasNext());
		assertEquals(Integer.valueOf(64), iter.next());

		// Check no more results
		assertFalse(iter.hasNext());
		assertTrue(cursorList.isFetchStarted());
		assertTrue(cursorList.isResultSetExhausted());
	}

	@Test(expected = IllegalStateException.class)
	public void testMoreThanOneIterator() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);

		CursorList<Integer> cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);
		int index = 0;
		// foreach statement : opens an iterator
		for (Integer i : cursorList) {
			assertEquals(Integer.valueOf(fakeResultList[index++]), i);
		}

		// foreach statement : opens another iterator
		for (Integer i : cursorList) {
			fail("I should't have reach this line");
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void testNoSuchElementException() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);

		CursorList<Integer> cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);

		Iterator<Integer> iter = cursorList.iterator();
		assertEquals(Integer.valueOf(16), iter.next());
		assertEquals(Integer.valueOf(32), iter.next());
		assertEquals(Integer.valueOf(64), iter.next());

		iter.next();
		fail("Previous iter.next should have failed");
	}

	public void testHasNextIdempotens() throws Exception {
		CursorResultSetHandler CursorResultSetHandler = getMockResultSetHandlerForResultList(fakeResultList);
		CursorList<Integer> cursorList = new CursorList(CursorResultSetHandler, getResultSetWrapper(), null, null);

		Iterator<Integer> iter = cursorList.iterator();
		assertTrue(iter.hasNext());
		assertTrue(iter.hasNext());
		assertEquals(Integer.valueOf(16), iter.next());
		assertTrue(iter.hasNext());
		assertTrue(iter.hasNext());
		assertEquals(Integer.valueOf(32), iter.next());
	}

	private CursorResultSetHandler getMockResultSetHandlerForResultList(final int[] fakeResultList)
			throws SQLException {
		CursorResultSetHandler cursorResultSetHandler = mock(CursorResultSetHandler.class);
    doAnswer(new Answer<Void>() {
      private int currentIndex = 0;

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        ((ResultHandler) args[2]).handleResult(new DefaultResultContext() {
          @Override
          public Object getResultObject() {
            if (currentIndex < fakeResultList.length) {
              return Integer.valueOf(fakeResultList[currentIndex++]);
            }
            return null;
          }
        });
        return null;
      }
    }).when(cursorResultSetHandler).handleRowValues(isNotNull(ResultSetWrapper.class), any(ResultMap.class),
				any(ResultHandler.class), any(RowBounds.class), any(ResultMapping.class));



		return cursorResultSetHandler;
	}

  private ResultSetWrapper getResultSetWrapper() {
    ResultSetWrapper resultSetWrapper = mock(ResultSetWrapper.class);
    when(resultSetWrapper.getResultSet()).thenReturn(mock(ResultSet.class));
    return resultSetWrapper;
  }
}
