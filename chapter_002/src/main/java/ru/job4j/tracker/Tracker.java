package ru.job4j.tracker;

import java.util.Random;

/**
 * Class Tracker repository of requests.
 * @author Roman Bednyashov (hipnorosva@gmail.com).
 * @since 0.1
 * @version 0.1
 */
public class Tracker {
    /**
     * Array if items.
     */
    private Item[] items = new Item[100];

    /**
     * Index of new item in array.
     * Object of random class.
     */
    private int position = 0;
    private static final Random RN = new Random();

    /**
     * Add item to array.
     * @param item input object of class item.
     * @return item.
     */
    public Item add(Item item) {
        item.setId(generatedId());
        this.items[this.position++] = item;
        return item;
    }

    /**
     * Changes the item in the array by the id.
     * @param id input id.
     * @param item input Item.
     */
    public void replace(String id, Item item) {
        for (int index = 0; index < this.position; index++) {
            if (items[index].getId().equals(id)) {
                this.items[index] = item;
                break;
            }
        }
    }

    /**
     * Search and delete item by id.
     * @param id input id of item.
     */
    public void delete(String id) {
        Item[] temp = new Item[position];
        int start = 0;
        for (int index = 0; index < this.position; index++) {
            if (items[index].getId().equals(id)) {
                items[index] = null;
                this.position--;
                start = index;
                break;
            } else {
                temp[index] = items[index];
            }
        }
        System.arraycopy(items, start + 1, temp, start, position - start);
        items[position] = null;
        System.arraycopy(temp, 0, items, 0, position);
    }

    /**
     * Finds all not null elements.
     * @return copy of array with all elements.
     */
    public Item[] findAll() {
        Item[] found = new Item[this.position];
        for (int index = 0; index < this.position; index++) {
            found[index] = this.items[index];
        }
        return found;
    }

    /**
     * Searches for items by name and copies them to an array - a list of names.
     * @param key input name to search.
     * @return list of elements with searched name.
     */
    public Item[] findByName(String key) {
        Item[] temp = new Item[position];
        int count = 0;
        for (int index = 0; index < position; index++) {
            if (this.items[index].getName().equals(key)) {
                temp[count] = this.items[index];
                count++;
            }
        }
        Item[] listOfNames = new Item[count];
        for (int index = 0; index < count; index++) {
            listOfNames[index] = temp[index];
        }
        return  listOfNames;
    }

    /**
     * Searches for items by id.
     * @param id input id to searching.
     * @return found item.
     */
    public Item findById(String id) {
        Item result = null;
        for (Item item : items) {
            if (item.getId().equals(id)) {
                result = item;
                break;
            }
        }
        return result;
    }

    /**
     * Generates a random number for a unique ID.
     * @return generated ID.
     */
    private String generatedId() {
        return String.valueOf(System.currentTimeMillis() + RN.nextInt());
    }
}
