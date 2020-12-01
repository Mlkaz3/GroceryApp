package com.example.groceryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.groceryapp.Adapter.CartItemAdapter
import com.example.groceryapp.Adapter.CartItemOnClickListener
import com.example.groceryapp.Adapter.MySingleton
import com.example.groceryapp.Model.CartItem
import com.example.groceryapp.Model.Product
import org.json.JSONArray
import org.json.JSONObject

class Cart : AppCompatActivity(), CartItemOnClickListener{

    //initialise product array list
    var userCartList = ArrayList<CartItem>()
    val adapter = CartItemAdapter(this,userCartList,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val backButton: ImageButton = findViewById(R.id.back2)
        backButton.setOnClickListener {
            finish()
        }

        //read the cart items from database
        userCartList = readCart()

        //access recyclerview UI
        val recyclerview: RecyclerView = findViewById(R.id.cart_rv)

        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        recyclerview.setHasFixedSize(true)

    }

    private fun readCart():ArrayList<CartItem> {
        //read from database
        //val url = getString(R.string.url_server) + getString(R.string.url_read_cart)

        //this code is use to read user 1 cart, where cart_id = 1
        val url = "https://groceryapptarucproject.000webhostapp.com/grocery/cart/readusercart.php?cart_id=1"
        val cartamount: TextView = findViewById(R.id.cartamount)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                // Process the JSON
                try{
                    if(response != null){
                        val strResponse = response.toString()
                        val jsonResponse  = JSONObject(strResponse)
                        val jsonArray: JSONArray = jsonResponse.getJSONArray("cart1")
                        val size: Int = jsonArray.length()
                        for(i in 0.until(size)){
                            var jsonCartItem: JSONObject = jsonArray.getJSONObject(i)

                            var cartitem: CartItem = CartItem(jsonCartItem.getInt("qty"),Product(jsonCartItem.getInt("product_id"),jsonCartItem.getString("product_name"),jsonCartItem.getDouble("product_price"),
                                    jsonCartItem.getString("product_category"), jsonCartItem.getString("product_img"),
                                    jsonCartItem.getInt("product_stock")))

                            userCartList.add(cartitem)
                        }
                        Toast.makeText(applicationContext, "Record found :$size", Toast.LENGTH_LONG).show()
                        cartamount.text = "$size total items"
                    }
                }catch (e:Exception){
                    Log.d("Main", "Response: %s".format(e.message.toString()))


                }
            },
            Response.ErrorListener { error ->
                Log.d("Main", "Response: %s".format(error.message.toString()))
            }
        )

        //Volley request policy, only one time request
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0, //no retry
            1f
        )

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

        return userCartList;
    }

    //credit to : https://www.youtube.com/watch?v=vz26K2xrO6I&feature=youtu.be
    override fun addQtyClicked(itemData: CartItem, position:Int) {
        itemData.productQty += 1
        adapter.notifyItemChanged(position)
        Log.e("cart changes", itemData.productQty.toString())

        //write the value to database
        updateQty(itemData)
    }

    override fun minusQtyClicked(itemData: CartItem, position:Int) {
        //if the qty is one then we will remove the whole thing from cart
        if(itemData.productQty == 1){
            Toast.makeText(this, "item delete from cart", Toast.LENGTH_LONG).show()
            //how to delete the record in view
       }else{
            itemData.productQty -= 1
        }
        adapter.notifyItemChanged(position)
        Log.e("cart changes", itemData.productQty.toString())

        //write the value to database
        updateQty(itemData)
    }

    //WORKING, BUT IF READ FROM DATABASE IS NOT UPDATE THEN HERE CONSIDER AS BUG
    fun updateQty(itemData: CartItem){
        //write to database(cart section), update cart item qty
        val url = "https://groceryapptarucproject.000webhostapp.com/grocery/cart/updatequantity.php?cart_id=1&product_id="+ itemData.productInfo.productID.toString() + "&qty=" + itemData.productQty
        val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    // Process the JSON
                    try{
                        if(response != null){
                            Toast.makeText(this, itemData.productInfo.productName + " quantity updated!", Toast.LENGTH_LONG).show()

                        }
                    }catch (e:Exception){
                        Log.d("Main", "Response: %s".format(e.message.toString()))

                    }
                },
                Response.ErrorListener { error ->
                    Log.d("Main", "Response: %s".format(error.message.toString()))

                })

        //Volley request policy, only one time request
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                0, //no retry
                1f
        )

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
}

