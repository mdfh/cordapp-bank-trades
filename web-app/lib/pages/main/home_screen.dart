import 'package:cordatradeclient/data/repository/trade_repo.dart';
import 'package:cordatradeclient/models/trade_state_model.dart';
import 'package:cordatradeclient/pages/dialog/bloc/dialog_bloc.dart';
import 'package:cordatradeclient/pages/dialog/dialog_provider.dart';
import 'package:cordatradeclient/pages/main/bloc/home_bloc.dart';
import 'package:cordatradeclient/pages/main/bloc/home_event.dart';
import 'package:cordatradeclient/pages/main/bloc/home_state.dart';
import 'package:cordatradeclient/pages/trade_detail/bloc/trade_detail_bloc.dart';
import 'package:cordatradeclient/pages/trade_detail/bloc/trade_detail_event.dart';
import 'package:cordatradeclient/pages/trade_detail/trade_detail_screen.dart';
import 'package:cordatradeclient/utils/ui_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key, required this.me}) : super(key: key);

  static void open(BuildContext context, String me) {
    final dialogBloc = DialogBloc();
    final page = MultiBlocProvider(
      providers: [
        BlocProvider(
          create: (_) => HomeBloc(
              me, RepositoryProvider.of<TradeRepository>(context), dialogBloc)
            ..add(LoadHomeEvent()),
        ),
      ],
      child: DialogProvider(
          bloc: dialogBloc,
          child: HomeScreen(
            me: me,
          )),
    );

    Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => page),
        (Route<dynamic> route) => false);
  }

  final String me;

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late TextEditingController amountController;
  late TextEditingController idController;

  @override
  void initState() {
    amountController = TextEditingController();
    idController = TextEditingController();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<HomeBloc, HomeState>(
        listener: (context, state) {},
        builder: (context, state) {
          if (state is HomeLoadedState) {
            return _body(context, state);
          } else
            return Center(child: CircularProgressIndicator());
        });
  }

  Widget _body(BuildContext context, HomeLoadedState state) {
    final width = MediaQuery.of(context).size.width / 4;

    return Scaffold(
        appBar: AppBar(
          title: Text("Trade Manager"),
          actions: [
            Container(
                width: width,
                child: TextField(
                  controller: idController,
                  decoration: InputDecoration(
                      fillColor: Colors.white,
                      focusColor: Colors.white,
                      hintText: "Search by linear id"),
                )),
            IconButton(
                icon: Icon(Icons.search),
                onPressed: () {
                  if (idController.text != null) {
                    openDetailScreen(id: idController.text);
                    idController.text = "";
                  }
                })
          ],
        ),
        body: Column(
          children: [
            Expanded(
                child: Column(
              children: [
                _submitTradeWidget(state),
                Expanded(child: getTradeList(state.trades))
              ],
            )),
            Divider(),
            Center(
                child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Text("You are logged In as ${state.me}",
                  style: TextStyle(fontSize: 18.0, color: Colors.blue)),
            )),
          ],
        ));
  }

  Widget getTradeList(List<TradeStateModel>? trades) {
    if (trades == null || trades.length == 0) {
      return Container();
    } else {
      return Padding(
        padding: const EdgeInsets.all(8.0),
        child: ListView.separated(
          itemCount: trades.length,
          shrinkWrap: true,
          physics: ClampingScrollPhysics(),
          itemBuilder: (context, i) {
            return _tradeItem(trades[i]);
          },
          separatorBuilder: (BuildContext context, int index) {
            return Divider();
          },
        ),
      );
    }
  }

  Widget _tradeItem(TradeStateModel model) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _tradeItem2("Trade Id", model.state.data.linearId),
        _tradeItem2("Amount", model.state.data.amount.toString()),
        _tradeItem2("Assigned To", model.state.data.assignedTo),
        _tradeItem2("Status", model.state.data.tradeStatus),
        IconButton(
            icon: Icon(Icons.arrow_right),
            onPressed: () {
              openDetailScreen(model: model);
            })
      ],
    );
  }

  Widget _tradeItem2(String key, String value) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.all(4.0),
          child: Text(key,
              style: TextStyle(fontSize: 15.0, fontWeight: FontWeight.bold)),
        ),
        Padding(
          padding: const EdgeInsets.all(4.0),
          child: SelectableText(value, style: TextStyle(fontSize: 18.0)),
        )
      ],
    );
  }

  Widget _submitTradeWidget(HomeLoadedState state) {
    final List<String> items = state.peers ?? [];
    final width = MediaQuery.of(context).size.width / 4;
    return Center(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Text("Create New Trade",
                style: TextStyle(fontSize: 15.0, fontWeight: FontWeight.bold)),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Container(
              width: width,
              child: DropdownButton<String>(
                isExpanded: true,
                items: items.map((String value) {
                  return new DropdownMenuItem<String>(
                    value: value,
                    child: new Text(value),
                  );
                }).toList(),
                value: state.selectedPeer,
                onChanged: (String? value) {
                  if (value != null)
                    context.read<HomeBloc>()
                      ..add(HomeEventSelectPeerEvent(value));
                },
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Container(
                width: width,
                child: TextField(
                    controller: amountController,
                    keyboardType: TextInputType.number,
                    decoration: InputDecoration(hintText: "Amount"))),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: ElevatedButton(
              child: Text("New Trade"),
              onPressed: () {
                if (state.selectedPeer == null) {
                  UiErrorUtils().openSnackBar(context, "Trade cannot be empty");
                  return;
                }

                if (amountController.text == null ||
                    amountController.text.isEmpty) {
                  UiErrorUtils()
                      .openSnackBar(context, "Amount cannot be empty");
                  return;
                }
                context.read<HomeBloc>()
                  ..add(HomeEventIssueTradeEvent(
                      int.parse(amountController.text)));
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: ElevatedButton(
              child: Text("Refresh"),
              onPressed: () {
                context.read<HomeBloc>()..add(LoadHomeEvent());
              },
            ),
          ),
        ],
      ),
    );
  }

  void openDetailScreen({TradeStateModel? model, String? id}) {
    if (model == null && id == null) return;

    final DialogBloc dialogBloc = DialogBloc();
    final page = BlocProvider(
      create: (_) => TradeDetailBloc(
          RepositoryProvider.of<TradeRepository>(context),
          dialogBloc,
          widget.me)
        ..add(model == null
            ? LoadTradeDetailByIdEvent(id!)
            : LoadTradeDetailEvent(model)),
      child: DialogProvider(
        bloc: dialogBloc,
        child: TradeDetailScreen(
            title: model == null ? id! : model.state.data.linearId),
      ),
    );

    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) {
        return page;
      }),
    );
  }
}
