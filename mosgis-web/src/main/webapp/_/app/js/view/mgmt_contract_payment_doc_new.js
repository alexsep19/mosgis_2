define ([], function () {

    return function (data, view) {
    
        var name = 'mgmt_contract_payment_common_service_payments_popup_form'

        var contract = data.contract || data
                
        $(view).w2popup('open', {

            width  : 605,
            height : 250,

            title   : 'Загрузка файла',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'files', type: 'file', options: {max: 1}},
                        ],                       

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_payment_common_service_payments_popup)                    

                }

            }

        });

    }

});