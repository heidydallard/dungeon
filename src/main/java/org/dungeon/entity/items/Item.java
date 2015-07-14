/*
 * Copyright (C) 2014 Bernardo Sulzbach
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dungeon.entity.items;

import org.dungeon.date.Date;
import org.dungeon.date.Period;
import org.dungeon.entity.Entity;
import org.dungeon.entity.Integrity;
import org.dungeon.entity.LightSource;
import org.dungeon.entity.Luminosity;
import org.dungeon.entity.TagSet;
import org.dungeon.entity.Weight;
import org.dungeon.game.Game;
import org.dungeon.game.Random;

public final class Item extends Entity {

  private final Integrity integrity;
  private final Date dateOfCreation;
  private final long decompositionPeriod;
  private final TagSet<Tag> tagSet;
  private final LightSource lightSource;
  private WeaponComponent weaponComponent;
  private FoodComponent foodComponent;
  private ClockComponent clockComponent;
  private BookComponent bookComponent;
  /* The Inventory this Item is in. Should be null whenever this Item is not in an Inventory. */
  private BaseInventory inventory;

  public Item(ItemBlueprint bp, Date date) {
    super(bp);

    tagSet = TagSet.copyTagSet(bp.tagSet);
    dateOfCreation = date;

    decompositionPeriod = bp.putrefactionPeriod;

    integrity = Integrity.makeIntegrity(bp.maxIntegrity, bp.curIntegrity, this);

    lightSource = new LightSource(bp.getLuminosity());

    if (hasTag(Tag.WEAPON)) {
      weaponComponent = new WeaponComponent(bp.damage, bp.hitRate, bp.integrityDecrementOnHit);
    }
    if (hasTag(Tag.FOOD)) {
      foodComponent = new FoodComponent(bp.nutrition, bp.integrityDecrementOnEat);
    }
    if (hasTag(Tag.CLOCK)) {
      clockComponent = new ClockComponent(this);
    }
    if (hasTag(Tag.BOOK)) {
      bookComponent = new BookComponent(bp.getSkill(), bp.text);
    }
  }

  @Override
  public Weight getWeight() {
    Weight weight = super.getWeight();
    if (hasTag(Tag.WEIGHT_PROPORTIONAL_TO_INTEGRITY)) {
      return weight.multiply(integrity.toPercentage());
    } else {
      return weight;
    }
  }

  /**
   * Returns how many seconds have passed since this Item was created.
   *
   * @return a long representing an amount of seconds
   */
  public long getAge() {
    Period existence = new Period(dateOfCreation, Game.getGameState().getWorld().getWorldDate());
    return existence.getSeconds();
  }

  public String getQualifiedName() {
    String singularName = getName().getSingular();
    if (getCurIntegrity() == getMaxIntegrity()) {
      return singularName;
    } else {
      return getIntegrityString() + " " + singularName;
    }
  }

  public boolean hasTag(Tag tag) {
    return tagSet.hasTag(tag);
  }

  public WeaponComponent getWeaponComponent() {
    return weaponComponent;
  }

  public FoodComponent getFoodComponent() {
    return foodComponent;
  }

  public ClockComponent getClockComponent() {
    return clockComponent;
  }

  public BookComponent getBookComponent() {
    return bookComponent;
  }

  public BaseInventory getInventory() {
    return inventory;
  }

  public void setInventory(BaseInventory inventory) {
    this.inventory = inventory;
  }

  private int getMaxIntegrity() {
    return integrity.getMaximum();
  }

  public int getCurIntegrity() {
    return integrity.getCurrent();
  }

  /**
   * Returns whether or not this item is broken.
   *
   * @return true if the current integrity is zero
   */
  public boolean isBroken() {
    return integrity.isBroken();
  }

  public void incrementIntegrity(int integrityIncrement) {
    integrity.incrementBy(integrityIncrement);
  }

  public void decrementIntegrityByHit() {
    integrity.decrementBy(weaponComponent.getIntegrityDecrementOnHit());
  }

  public void decrementIntegrityByEat() {
    integrity.decrementBy(foodComponent.getIntegrityDecrementOnEat());
  }

  public void decrementIntegrityToZero() {
    integrity.decrementBy(integrity.getCurrent());
  }

  /**
   * Rolls for a hit.
   *
   * @return true if the next attack should hit, false otherwise
   */
  public boolean rollForHit() {
    return Random.roll(weaponComponent.getHitRate());
  }

  private String getIntegrityString() {
    return IntegrityState.getIntegrityState(getCurIntegrity(), getMaxIntegrity()).toString();
  }

  public long getDecompositionPeriod() {
    return decompositionPeriod;
  }

  @Override
  public Luminosity getLuminosity() {
    return lightSource.getLuminosity();
  }

  @Override
  public String toString() {
    return getName().toString();
  }

  public enum Tag {WEAPON, FOOD, CLOCK, BOOK, DECOMPOSES, REPAIRABLE, WEIGHT_PROPORTIONAL_TO_INTEGRITY}

}
