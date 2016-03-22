package com.syncobjects.as.optimizer;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.syncobjects.as.api.Converter;

public class SimpleDateConverter implements Converter<Date> {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	@Override
	public Date convert(String[] values) throws Exception {
		Date date = sdf.parse(values[0]);
		return date;
	}
}
