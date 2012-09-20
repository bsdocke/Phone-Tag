package com.android.data;

import java.util.Date;

public class GameListItem {

	private String gameName;
	private Date gameBeginDate;

	public GameListItem(String name, Date begin) {

		gameName = name;
		gameBeginDate = begin;
	}

	public String getName() {
		return gameName;
	}

	public Date getDate() {
		return gameBeginDate;
	}

	public void setName(String newName) {
		gameName = newName;
	}

	public void setDate(Date newDate) {
		gameBeginDate = newDate;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {

		// Make sure there really IS another object:
		if (o == null) {
			return false;
		}
		// Make sure it's of the correct type:
		if (!this.getClass().equals(o.getClass())) {
			return false;
		}

		GameListItem otherItem = (GameListItem) o;
		return (otherItem.gameName.equals(this.gameName));// && otherItem.gameBeginDate.equals(this.gameBeginDate));

	}

}
