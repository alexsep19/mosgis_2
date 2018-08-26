define ([], function () {

    return function (data, view) {
        
        $(view).w2popup ('open', {

            width  : 605,
            height : 280,

            title  : 'Страховой продукт',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'insurance_product_popup_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({
                    
                        record: data.record,

                        name: name,

                        fields : [
                            {name: 'description',  type: 'textarea'},
                            {name: 'files', type: 'file', options: {max: 1}},
                            {name: 'insuranceorg', type: 'list', options: {items: data.vc_orgs_ins.items.concat ({id: 'other', text: '(другая организация...)'})}},
                        ],
                        
                        focus: data.record.insuranceorg ? 1 : 0,
                        
                        onChange: function (e) {

                            if (e.target == 'insuranceorg' && e.value_new.id == 'other') $_DO.open_orgs_insurance_product_popup ()

                        }

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_insurance_product_popup)

                }

            }

        })

    }    

})