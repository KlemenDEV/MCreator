/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2023, Pylo, opensource contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.java.debug;

import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.WatchpointRequest;

import javax.annotation.Nullable;

public class Watchpoint {

	private final String classname;
	private final String fieldname;

	private boolean loaded = false;

	@Nullable private WatchpointListener listener;

	@Nullable private WatchpointRequest watchpointRequest = null;

	public Watchpoint(String classname, String fieldname, @Nullable WatchpointListener listener) {
		this.classname = classname;
		this.fieldname = fieldname;
		this.listener = listener;
	}

	public String getClassname() {
		return classname;
	}

	public String getFieldname() {
		return fieldname;
	}

	public void setWatchpointRequest(@Nullable WatchpointRequest watchpointRequest) {
		this.watchpointRequest = watchpointRequest;
	}

	@Nullable public WatchpointRequest getWatchpointRequest() {
		return watchpointRequest;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Watchpoint that = (Watchpoint) o;

		return classname.equals(that.classname) && fieldname.equals(that.fieldname);
	}

	@Override public int hashCode() {
		return (classname + "." + fieldname).hashCode();
	}

	public void setListener(@Nullable WatchpointListener listener) {
		this.listener = listener;
	}

	@Nullable public WatchpointListener getListener() {
		return listener;
	}

	public interface WatchpointListener {

		void watchpointLoaded(Watchpoint watchpoint);

		void watchpointModified(Watchpoint watchpoint, ModificationWatchpointEvent event);

	}

	protected boolean isLoaded() {
		return loaded;
	}

	protected void setLoaded(boolean loaded) {
		if (!this.loaded && loaded) {
			if (listener != null) {
				listener.watchpointLoaded(this);
			}
		}

		this.loaded = loaded;
	}

}
