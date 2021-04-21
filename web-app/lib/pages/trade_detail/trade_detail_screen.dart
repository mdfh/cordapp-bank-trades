import 'package:cordatradeclient/pages/trade_detail/bloc/trade_detail_bloc.dart';
import 'package:cordatradeclient/pages/trade_detail/bloc/trade_detail_state.dart';
import 'package:cordatradeclient/utils/consts.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'bloc/trade_detail_event.dart';

class TradeDetailScreen extends StatelessWidget {
  final String title;

  const TradeDetailScreen({Key? key, required this.title}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<TradeDetailBloc, TradeDetailState>(
        builder: (context, state) {
          if (state is TradeDetailLoadedState)
            return _body(context, state);
          else
            return Container();
        },
        listener: (context, state) {});
  }

  Widget _body(BuildContext context, TradeDetailLoadedState state) {
    final width = MediaQuery.of(context).size.width / 4;
    return Scaffold(
      appBar: AppBar(
        title: Text(title),
      ),
      body: Column(
        children: [
          Table(
            border: TableBorder.all(
                color: Colors.grey, style: BorderStyle.solid, width: 2),
            children: [
              TableRow(children: [
                Column(children: [
                  _label("Linear ID"),
                  _label("Amount"),
                  _label("Assigned By"),
                  _label("Assigned To"),
                  _label("Date"),
                  _label("Status"),
                  _label("Notary"),
                  _label("Hash"),
                ]),
                Column(children: [
                  _value(state.model.state.data.linearId),
                  _value(state.model.state.data.amount.toString()),
                  _value(state.model.state.data.assignedBy),
                  _value(state.model.state.data.assignedTo),
                  _value(state.model.state.data.date),
                  _value(state.model.state.data.tradeStatus),
                  _value(state.model.state.notary),
                  _value(state.model.ref.txhash),
                ]),
              ]),
            ],
          ),
          SizedBox(
            height: 24,
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text("You can change the status of trade here : "),
              SizedBox(
                width: 16,
              ),
              Container(
                width: width,
                child: ElevatedButton(
                  child: Text("In Process"),
                  onPressed: state.isLoading ||
                          state.model.state.data.tradeStatus !=
                              AppConstants.TRADE_SUBMITTED
                      ? null
                      : () {
                          context
                              .read<TradeDetailBloc>()
                              .add(TradeDetailMarkInProcessEvent());
                        },
                ),
              ),
              SizedBox(
                width: 16,
              ),
              Container(
                width: width,
                child: ElevatedButton(
                  child: Text("Settled"),
                  onPressed: state.isLoading ||
                          state.model.state.data.tradeStatus !=
                              AppConstants.TRADE_IN_PROCESS
                      ? null
                      : () {
                          context
                              .read<TradeDetailBloc>()
                              .add(TradeDetailMarkSettledEvent());
                        },
                ),
              ),
              SizedBox(
                width: 16,
              ),
              Visibility(
                  visible: state.isLoading, child: CircularProgressIndicator())
            ],
          )
        ],
      ),
    );
  }

  Widget _label(String label) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Text(label, style: TextStyle(fontSize: 18.0, fontWeight: FontWeight.bold)),
    );
  }

  Widget _value(String value) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Text(value, style: TextStyle(fontSize: 18.0)),
    );
  }
}
