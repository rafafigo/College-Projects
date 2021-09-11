package pt.ulisboa.tecnico.muc.shopist.domain;

import com.google.firebase.Timestamp;

public class PantryItem extends DomainComparator {

  private Integer inPantry;
  private Integer inNeed;
  private Integer inCart;

  public PantryItem(String pantryId, Timestamp timestamp) {
    super(pantryId, timestamp);
    this.inCart = 0;
  }

  public PantryItem(String pantryId, Integer inPantry, Integer inNeed, Timestamp timestamp) {
    super(pantryId, timestamp);
    this.inPantry = inPantry;
    this.inNeed = inNeed;
    this.inCart = 0;
  }

  public PantryItem(
      String pantryId, Integer inPantry, Integer inNeed, Integer inCart, Timestamp timestamp) {
    super(pantryId, timestamp);
    this.inPantry = inPantry;
    this.inNeed = inNeed;
    this.inCart = inCart;
  }

  public PantryItem(PantryItem pantryItem) {
    super(pantryItem.getId(), pantryItem.getTimestamp());
    this.inPantry = pantryItem.getInPantry();
    this.inNeed = pantryItem.getInNeed();
    this.inCart = pantryItem.getInCart();
  }

  /* Quantity in Pantry */

  public Integer getInPantry() {
    return inPantry;
  }

  public void setInPantry(int inPantry) {
    this.inPantry = inPantry;
  }

  /* Quantity in Need */

  public Integer getInNeed() {
    return inNeed;
  }

  public void setInNeed(int inNeed) {
    this.inNeed = inNeed;
  }

  public boolean hasInNeed() {
    return this.inNeed > 0;
  }

  /* Quantity in Cart */

  public Integer getInCart() {
    return inCart;
  }

  public void setInCart(int inCart) {
    this.inCart = inCart;
  }

  public void setInCartInNeed(int inCart) {
    this.inNeed += this.inCart - inCart;
    this.inCart = inCart;
  }

  public boolean hasInCart() {
    return this.inCart > 0;
  }
}
