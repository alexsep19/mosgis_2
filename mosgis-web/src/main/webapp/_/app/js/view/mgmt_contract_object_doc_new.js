define ([], function () {

    return function (data, view) {

        var contract = data.contract || data
        
        var types = {
            99: 1,
        }
        
        switch (data.item ['ctr.code_vc_nsi_58']) {

            case "1": 
                if (data.item.id_customer_type != 1) types [10] = types [8] = 1;
                break;

            case "9": 
                types [3] = 1;
                break;

        }        

        $(view).w2popup('open', {

            width  : 605,
            height : 250,

            title   : 'Добавление документа',

            onOpen: function (event) {
            
                var ts = data.vc_contract_doc_types.items.filter (function (i) {return types [i.id]})
                
                var record = {}

                if (ts.length == 1) record.id_type = ts [0]

                event.onComplete = function () {

                    var name = 'mgmt_contract_object_doc_new_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,
                        
                        record: record,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'files', type: 'file', options: {max: 1}},
                            {name: 'id_type', type: 'list', options: {items: ts}},
                        ],                       
                        
                        focus: -1,

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_object_doc_new)

                }

            }

        });

    }

});