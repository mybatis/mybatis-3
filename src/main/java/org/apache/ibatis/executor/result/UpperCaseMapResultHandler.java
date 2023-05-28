package org.apache.ibatis.executor.result;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.ResultContext;

/*
 * this result handler make sure that we get the keys of the result map in upper case
 */
public class UpperCaseMapResultHandler extends DefaultResultHandler {

	private boolean fromCache = false;

	public UpperCaseMapResultHandler() {
		super();
	}

	public UpperCaseMapResultHandler(ObjectFactory objectFactory) {
		super(objectFactory);
	}

	@Override
	public void handleResult(ResultContext<? extends Object> context) {

		Object result = context.getResultObject();
		if (result != null) {
			super.getResultList().add(buildUpperCasekeyMap(result));
			return;
		}

		super.handleResult(context);
	}

	public static Object buildUpperCasekeyMap(Object result) {
		if (result != null && Map.class.isAssignableFrom(result.getClass())) {
			Map<?, ?> map = (Map<?, ?>) result;
			return map.keySet().stream().collect(Collectors.toMap(key -> ((String) key).toUpperCase(), key -> map.get(key)));
		}
		return result;
	}

	public boolean isFromCache() {
		return fromCache;
	}

	public void setFromCache(boolean fromCache) {
		this.fromCache = fromCache;
	}

}
