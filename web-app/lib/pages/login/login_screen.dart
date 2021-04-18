import 'package:cordatradeclient/pages/login/bloc/login_bloc.dart';
import 'package:cordatradeclient/pages/login/bloc/login_event.dart';
import 'package:cordatradeclient/pages/login/bloc/login_state.dart';
import 'package:cordatradeclient/pages/main/home_screen.dart';
import 'package:cordatradeclient/utils/ui_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LoginScreen extends StatefulWidget {
  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final domainController = TextEditingController();
  final portController = TextEditingController();
  final usernameController = TextEditingController();
  final passwordController = TextEditingController();

  @override
  void initState() {
    domainController.text = "localhost";
    portController.text = "8086";
    usernameController.text = "user1";
    passwordController.text = "test";
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocConsumer<LoginBloc, LoginState>(listener: (context, state) {
        if (state is LoginSuccessState) {
          HomeScreen.open(context, state.me);
        } else if (state is LoginErrorState) {
          UiErrorUtils().openSnackBar(context, "Failed to login");
        }
      }, builder: (context, state) {
        return Center(
          child: Container(
            width: 500,
            height: 300,
            decoration: BoxDecoration(
              border: Border.all(
                color: Colors.grey,
                width: 2.0,
              ),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Row(
                    children: [
                      Text("http://"),
                      SizedBox(width: 4,),
                      Flexible(
                        flex: 2,
                        child: TextField(
                          controller: domainController,
                          decoration: InputDecoration(hintText: "localhost"),
                        ),
                      ),
                      SizedBox(width: 4,),
                      Text(":"),
                      SizedBox(width: 4,),
                      Flexible(
                        flex: 1,
                        child: TextField(
                          controller: portController,
                          decoration: InputDecoration(hintText: "port"),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 4,),
                  TextField(
                    controller: usernameController,
                    decoration: InputDecoration(hintText: "Username"),
                  ),
                  SizedBox(height: 4,),
                  TextField(
                    controller: passwordController,
                    obscureText: true,
                    decoration: InputDecoration(hintText: "Password",),
                  ),
                  SizedBox(height: 16,),
                  Container(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: state is LoginLoadingState
                          ? null
                          : () {
                              if (domainController.text.isEmpty ||
                                  portController.text.isEmpty ||
                                  usernameController.text.isEmpty ||
                                  passwordController.text.isEmpty) {
                                UiErrorUtils()
                                    .openSnackBar(context, "Invalid Input");
                                return;
                              }
                              context.read<LoginBloc>().add(LoginInitEvent(
                                  domainController.text,
                                  portController.text,
                                  usernameController.text,
                                  passwordController.text));
                            },
                      child: Text("Login"),
                    ),
                  )
                ],
              ),
            ),
          ),
        );
      }),
    );
  }
}
