define ([], function () {

    return function (data, view) {
    
        var name = 'mgmt_contract_doc_new_form'

        function recalc () {

            var v = w2ui [name].values ()

            var on = v.id_type == 9 ? 1 : 0

            var $purchasenumber = $('#purchasenumber')
            var $row = $purchasenumber.closest ('.w2ui-field')

            if (on) {
                $row.show ()
                $purchasenumber.prop ('disabled', false).focus ()
            }
            else {
                $purchasenumber.val ('').prop ('disabled', true)
                $row.hide ()
            }
                        
            var o = {
                form: 195,
                page: 116,
                box: 216,
                popup: 250,
                'form-box': 193,
            }
            
            for (var k in o) $row.closest ('.w2ui-' + k).height (o [k] + 30 * on)
            
        }

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

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                            {name: 'purchasenumber',  type: 'text' },
                            {name: 'files', type: 'file', options: {max: 1}},
                            {name: 'id_type', type: 'list', options: {items: data.vc_contract_doc_types.items.filter (function (i) {return types [i.id]}) }},
                        ],
                        
                        onChange: function (e) {if (e.target == "id_type") e.done (recalc)},
                        
                        onRender: function (e) {e.done (setTimeout (recalc, 100))}

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_doc_new)

                }

            }

        });

    }

});