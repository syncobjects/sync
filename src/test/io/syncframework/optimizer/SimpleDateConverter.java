package io.syncframework.optimizer;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.syncframework.api.Converter;

public class SimpleDateConverter implements Converter<Date> {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	@Override
	public Date convert(String[] values) throws Exception {
		Date date = sdf.parse(values[0]);
		return date;
	}
}
