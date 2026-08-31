package com.rimuru.tempestarea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay

data class PS(val id:Int, val name:String, val active:Boolean=false, val start:Long=0)
data class Menu(val name:String,val price:Int,val cat:String)
data class Cart(val menu:Menu,val qty:Int=1)

class Store:ViewModel(){
 var ps by mutableStateOf(List(6){PS(it+1,"PS3 ${it+1}")})
 var cart by mutableStateOf(listOf<Cart>())
 var psIncome by mutableIntStateOf(0)
 var foodIncome by mutableIntStateOf(0)
 var cashIncome by mutableIntStateOf(0)
 var qrisIncome by mutableIntStateOf(0)
 val menus=listOf(
  Menu("Sop Iga",25000,"Makanan"),Menu("Ayam Bakar",15000,"Makanan"),
  Menu("Ayam Goreng",15000,"Makanan"),Menu("Nila Bakar",15000,"Makanan"),
  Menu("Cumi Bakar",20000,"Makanan"),Menu("Nila Goreng",15000,"Makanan"),
  Menu("Mie Goreng",15000,"Makanan"),Menu("Nasi Goreng",15000,"Makanan"),
  Menu("Es Jeruk",5000,"Minuman"),Menu("Es Teh Manis",5000,"Minuman"),
  Menu("Kopi",5000,"Minuman"),Menu("Pop Ice",5000,"Minuman"),Menu("Jus",5000,"Minuman")
 )
 fun start(id:Int){ps=ps.map{if(it.id==id)it.copy(active=true,start=System.currentTimeMillis())else it}}
 fun stop(id:Int):Int{
  val x=ps.first{it.id==id}; val sec=((System.currentTimeMillis()-x.start)/1000).coerceAtLeast(60)
  val fee=kotlin.math.ceil(sec/3600.0*5000).toInt()
  ps=ps.map{if(it.id==id)it.copy(active=false,start=0)else it}; return fee
 }
 fun add(m:Menu){val i=cart.indexOfFirst{it.menu.name==m.name};cart=if(i<0)cart+Cart(m)else cart.mapIndexed{n,x->if(n==i)x.copy(qty=x.qty+1)else x}}
 fun qty(name:String,d:Int){cart=cart.mapNotNull{if(it.menu.name==name){val q=it.qty+d;if(q<=0)null else it.copy(qty=q)}else it}}
 fun total()=cart.sumOf{it.menu.price*it.qty}
 fun pay(method:String,rental:Int){val f=total();psIncome+=rental;foodIncome+=f;if(method=="Tunai")cashIncome+=rental+f else qrisIncome+=rental+f;cart=emptyList()}
}
fun rp(x:Int)=NumberFormat.getCurrencyInstance(Locale("id","ID")).format(x).replace(",00","")

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{App()}}}

@Composable fun App(vm:Store=viewModel()){
 var page by remember{mutableStateOf("login")};var rental by remember{mutableIntStateOf(0)}
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF1557B0),secondary=Color(0xFFFF7A00))){
  when(page){
   "login"->Login{page="home"}
   "home"->Home(vm,{page="rental"},{page="food"},{page="report"})
   "rental"->Rental(vm,{page="home"},{page="food"},{rental=it;page="pay"})
   "food"->Food(vm,{page="home"},{page="cart"})
   "cart"->CartScreen(vm,{page="food"},{page="pay"})
   "pay"->Pay(vm,rental,{page="home";rental=0})
   "report"->Report(vm){page="home"}
  }
 }
}
@Composable fun Login(ok:()->Unit){var u by remember{mutableStateOf("")};var p by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.Center){
  Text("RIMURU\nTEMPEST AREA",style=MaterialTheme.typography.displaySmall,color=Color(0xFF1557B0))
  Text("RENTAL PS3 & RUMAH MAKAN");Spacer(Modifier.height(24.dp))
  OutlinedTextField(u,{u=it},label={Text("Username")},modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(10.dp));OutlinedTextField(p,{p=it},label={Text("Password")},modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(16.dp));Button(ok,Modifier.fillMaxWidth().height(52.dp)){Text("LOGIN")}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){AssistChip(ok,{Text("👑 Admin")});AssistChip(ok,{Text("👤 Karyawan")})}
 }}
@Composable fun Home(vm:Store,r:()->Unit,f:()->Unit,l:()->Unit){
 Scaffold(topBar={TopAppBar(title={Text("Dashboard Rimuru Tempest Area")})}){pad->
  LazyColumn(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   item{Text("Halo, Admin 👋",style=MaterialTheme.typography.headlineSmall)}
   item{Row{Card(Modifier.weight(1f).padding(4.dp)){Column(Modifier.padding(14.dp)){Text("Pendapatan");Text(rp(vm.psIncome+vm.foodIncome),style=MaterialTheme.typography.titleLarge)}};Card(Modifier.weight(1f).padding(4.dp)){Column(Modifier.padding(14.dp)){Text("Rental Aktif");Text(vm.ps.count{it.active}.toString(),style=MaterialTheme.typography.titleLarge)}}}}
   item{Button(r,Modifier.fillMaxWidth()){Icon(Icons.Default.SportsEsports,null);Spacer(Modifier.width(8.dp));Text("RENTAL PS3")}}
   item{Button(f,Modifier.fillMaxWidth()){Icon(Icons.Default.Restaurant,null);Spacer(Modifier.width(8.dp));Text("RUMAH MAKAN")}}
   item{OutlinedButton(l,Modifier.fillMaxWidth()){Icon(Icons.Default.BarChart,null);Spacer(Modifier.width(8.dp));Text("LAPORAN")}}
   item{Text("Status 6 PS3",style=MaterialTheme.typography.titleLarge)}
   items(vm.ps){x->Card(Modifier.fillMaxWidth().clickable{r()}){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.SportsEsports,null,Modifier.size(40.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(x.name);Text(if(x.active)"🔴 TERPAKAI" else "🟢 TERSEDIA")}}}}
  }
 }}
@Composable fun Rental(vm:Store,back:()->Unit,food:()->Unit,pay:(Int)->Unit){var now by remember{mutableLongStateOf(System.currentTimeMillis())};LaunchedEffect(Unit){while(true){delay(1000);now=System.currentTimeMillis()}}
 Scaffold(topBar={TopAppBar(title={Text("Rental PS3")},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,null)}})}){pad->
  LazyColumn(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(vm.ps){x->
   val sec=if(x.active)((now-x.start)/1000).coerceAtLeast(0)else 0
   Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text(x.name,style=MaterialTheme.typography.titleLarge);Text(if(x.active)"🔴 TERPAKAI • ${sec/3600}:${(sec%3600)/60}:${sec%60}" else "🟢 TERSEDIA");Spacer(Modifier.height(8.dp))
    if(!x.active)Button({vm.start(x.id)},Modifier.fillMaxWidth()){Text("MULAI RENTAL")}
    else Row{OutlinedButton(food,Modifier.weight(1f)){Text("TAMBAH PESANAN")};Spacer(Modifier.width(8.dp));Button({pay(vm.stop(x.id))},Modifier.weight(1f)){Text("AKHIRI RENTAL")}}
   }}
  }}
 }}
@Composable fun Food(vm:Store,back:()->Unit,cart:()->Unit){var tab by remember{mutableStateOf("Semua")};val data=if(tab=="Semua")vm.menus else vm.menus.filter{it.cat==tab}
 Scaffold(topBar={TopAppBar(title={Text("Rumah Makan")},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,null)}},actions={IconButton(cart){BadgedBox(badge={if(vm.cart.isNotEmpty())Badge{Text(vm.cart.sumOf{it.qty}.toString())}}){Icon(Icons.Default.ShoppingCart,null)}}})}){pad->
  Column(Modifier.padding(pad)){Row(Modifier.padding(10.dp)){listOf("Semua","Makanan","Minuman").forEach{FilterChip(tab==it,{tab=it},{Text(it)},Modifier.padding(end=6.dp))}}
   LazyColumn(Modifier.padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){items(data){m->Card(Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(m.cat=="Makanan")Icons.Default.Restaurant else Icons.Default.LocalDrink,null);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(m.name);Text(rp(m.price))};FilledIconButton({vm.add(m)}){Icon(Icons.Default.Add,null)}}}}}
  }
 }}
@Composable fun CartScreen(vm:Store,back:()->Unit,pay:()->Unit){Scaffold(topBar={TopAppBar(title={Text("Keranjang Pesanan")},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,null)}})}){pad->
 Column(Modifier.padding(pad).padding(16.dp)){if(vm.cart.isEmpty())Text("Keranjang masih kosong")else{LazyColumn(Modifier.weight(1f)){items(vm.cart){x->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(x.menu.name);Text(rp(x.menu.price*x.qty))};IconButton({vm.qty(x.menu.name,-1)}){Icon(Icons.Default.Remove,null)};Text(x.qty.toString());IconButton({vm.qty(x.menu.name,1)}){Icon(Icons.Default.Add,null)}}}}};Text("TOTAL: ${rp(vm.total())}",style=MaterialTheme.typography.headlineSmall);Button(pay,Modifier.fillMaxWidth()){Text("LANJUT KE PEMBAYARAN")}}}
 }}
@Composable fun Pay(vm:Store,rental:Int,done:()->Unit){var method by remember{mutableStateOf("Tunai")};var cash by remember{mutableStateOf("")};val total=vm.total()+rental
 Scaffold(topBar={TopAppBar(title={Text("Pembayaran")})}){pad->Column(Modifier.padding(pad).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("TOTAL TAGIHAN");Text(rp(total),style=MaterialTheme.typography.displaySmall,color=Color(0xFF1557B0));Row{FilterChip(method=="Tunai",{method="Tunai"},{Text("💵 Tunai")});Spacer(Modifier.width(8.dp));FilterChip(method=="QRIS",{method="QRIS"},{Text("📱 QRIS")})};Spacer(Modifier.height(16.dp))
  if(method=="Tunai"){OutlinedTextField(cash,{cash=it.filter(Char::isDigit)},label={Text("Uang diterima")});val got=cash.toIntOrNull()?:0;Text("Kembalian: ${rp((got-total).coerceAtLeast(0))}");Button({vm.pay("Tunai",rental);done()},Modifier.fillMaxWidth()){Text("PEMBAYARAN LUNAS")}}
  else{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.QrCode,null,Modifier.size(180.dp));Text("QRIS RIMURU TEMPEST AREA",style=MaterialTheme.typography.titleMedium);Text("Nominal: ${rp(total)}");Text("Pelanggan scan QRIS dan bayar sesuai total.")}};Spacer(Modifier.height(16.dp));Button({vm.pay("QRIS",rental);done()},Modifier.fillMaxWidth()){Text("SAYA SUDAH BAYAR")}}
 }}
}
@Composable fun Report(vm:Store,back:()->Unit){Scaffold(topBar={TopAppBar(title={Text("Laporan")},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,null)}})}){pad->Column(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Stat("Pendapatan Rental PS",rp(vm.psIncome));Stat("Pendapatan Rumah Makan",rp(vm.foodIncome));Stat("Tunai",rp(vm.cashIncome));Stat("QRIS",rp(vm.qrisIncome));Stat("TOTAL",rp(vm.psIncome+vm.foodIncome))}}}
@Composable fun Stat(a:String,b:String){Card(Modifier.fillMaxWidth()){Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a);Text(b,style=MaterialTheme.typography.titleLarge)}}}
