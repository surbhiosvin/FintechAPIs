<?php
error_reporting(0);
ini_set('display_errors',0);

defined('BASEPATH') OR exit('No direct script access allowed');

require(APPPATH.'/libraries/REST_Controller.php');
require(APPPATH.'/libraries/stripe.php');
/**
* This is an example of a few basic user interaction methods you could use
* all done with a hardcoded array
*
* @package         CodeIgniter
* @subpackage      Rest Server
* @category        Controller
*
**/
class User extends REST_Controller {
  function __construct() {
    parent::__construct();
        $this->methods['user_post']['limit'] = 100; // 100 requests per hour per user/key
        $this->methods['user_delete']['limit'] = 50; // 50 requests per hour per user/key
        $this->load->model('User_model');

        $this->load->library('email');
        $this->load->library('form_validation');
        $this->load->database();
        Stripe\Stripe::setApiKey("sk_test_0HEIX5gE8pRO5X5xuSPrPczU");

    }

     public function signup_post(){
          $email=$this->input->post('email');
          $password=$this->input->post('password');
          $signup_type=$this->input->post('signup_type');
          $fb_id=$this->input->post('fb_id');
          $signup_level=$this->input->post('signup_level');
          $first_name=$this->input->post('first_name');
          $last_name=$this->input->post('last_name');

          if(empty($signup_level))
          {
               $response= $this->User_model->errorResponse("Required fields are missing");
          }
               
               $public_token=$this->input->post('public_token');
               $institution_id=$this->input->post('institution_id');
               if(empty($public_token))
               {
                    $response= $this->User_model->errorResponse("Required fields are missing");
               }else
               {
                    $vars = array(
                   'client_id'=>'PlaidClientID',
                   'secret'=>'PlaidSecret',
                   'public_token'=>$public_token
                    );
                    $aa=json_encode($vars);
                    $ch = curl_init();
                    curl_setopt($ch, CURLOPT_URL,"PlaidExchangeURL");
                    curl_setopt($ch, CURLOPT_POST, 1);
                    curl_setopt($ch, CURLOPT_POSTFIELDS,$aa);  
                    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                    $headers = [
                    'Content-Type: application/json'
                    ];  
                    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
                    $server_output = curl_exec ($ch);
                    print_r($server_output);die;
                    curl_close ($ch);
                    $getaccesstoken=json_decode($server_output);
                    $access_token=$getaccesstoken->access_token;
                    $date=date('Y-m-d');
                    $vars12 = array(
                     'client_id'=>'PlaidClientID',
                     'secret'=>'PlaidSecret',
                    'access_token'=>$access_token,
                    "start_date"=> "2010-05-20",
                    "end_date"=> $date,
                    );
                    $aa2=json_encode($vars12);
                    
                    $myarray = array(
                    'plaid_public_token'=>$public_token,
                    'plaid_access_token'=>$access_token,
                    'signup_level'=>$signup_level,
                    'plaid_ins_id'=>$institution_id,
                    );
                    $response = $this->User_model->updateprofile($myarray,$user_id);
               } 
          
          $this->set_response($response, REST_Controller::HTTP_OK);
          }


     public function createAccountId_post(){
          $user_id=$this->input->post('user_id');
          $access_token=$this->input->post('access_token');
          $account_id=$this->input->post('account_id');
          if (empty($user_id) || empty($access_token) || empty($account_id) ) {
               $response= $this->User_model->errorResponse("Required fields are missing");
          }
          else{
               $vars12 = array(
              'client_id'=>'PlaidClientID',
              'secret'=>'PlaidSecret',
               'access_token'=>$access_token,
               'account_id'=>$account_id
               );

               $aa2=json_encode($vars12);
               $url="CreateBankTokenURL";
               $result=curl_function($aa2,$url);
               // print_r($result);die;

               if (isset($result->stripe_bank_account_token)) {
                    $stripe_token=$result->stripe_bank_account_token;
                    $customer = \Stripe\Customer::create(array(
                    "source" => $stripe_token,
                    "description" => "work")
                    );
                    $customer_id = $customer->id;
                    $myarray = array(
                    'plaid_access_token'=>$access_token,
                    'account_ids'=> $account_id,
                    'stripe_token'=>$stripe_token,
                    'stripe_customer_id'=>$customer_id
                    );
                    $response =$this->User_model->updateprofile($myarray,$user_id);
               }
               else{
                    $response= $this->User_model->errorResponse($result->error_message);
               }
          }
          $this->set_response($response, REST_Controller::HTTP_OK);
    }


    public function createaccesstoken_post(){
        $public_token = $this->input->post('public_token');
        $institution_id = $this->input->post('institution_id');
        $user_id = $this->input->post('user_id');
        if(empty($public_token) ||  empty($institution_id)  || empty($user_id))
        {
            $response= $this->User_model->errorResponse("Required fields are missing");
        }else
        {
            
            $params = array(
            'client_id'=>'PlaidClientID',
            'secret'=>'PlaidSecret',
            'public_token'=>$public_token
            );
            $params=json_encode($params);
            $url="ExchangeURL";
            $getaccesstoken=curl_function($params,$url);
            $access_token=$getaccesstoken->access_token;
           
            $response = $this->User_model->updateprofile($myarray,$user_id);
            }
            else{
                $response= $this->User_model->errorResponse("Error in creating token.");
            }
            
        } 
        $this->set_response($response, REST_Controller::HTTP_OK);
    } 


  }
