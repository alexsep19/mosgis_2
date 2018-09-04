define ([], function () {

    return function (data, view) {

        var contract = data.contract || data
        
        var types = {
            4:  1,
            5:  1,
            99: 1,
        }
        
        switch (data.item.code_vc_nsi_58) {

            case "1": 
                if (data.item.id_customer_type == 1) types [10] = types [8] = 1;
                break;

            case "2": 
                types [9] = 1;
                break;

            case "10": 
                types [2] = 1;
                break;

            case "5": 
                types [7] = 1;
                break;

            case "6": 
                types [6] = 1;
                break;

        }        

        $(view).w2popup('open', {

            width  : 605,
            height : 280,

            title   : 'Добавление документа',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'mgmt_contract_doc_new_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'purchasenumber',  type: 'text' },
                            {name: 'files', type: 'file', options: {max: 1}},
                            {name: 'id_type', type: 'list', options: {items: data.vc_contract_doc_types.items.filter (function (i) {return types [i.id]}) }},
                        ],
                        
                        onChange: function (e) {
                        
                            if (e.target == "id_type") e.done (function () {
                            
                                var $purchasenumber = $('#purchasenumber')
                                
                                if (e.value_new.id == 9) {
                                    $purchasenumber.prop ('disabled', false).focus ()
                                }
                                else {
                                    $purchasenumber.val ('').prop ('disabled', true)
                                }
                                                        
                            })
                        
                        }

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_doc_new)

                }

            }

        });

    }

});