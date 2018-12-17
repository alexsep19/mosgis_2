define ([], function () {

    return function (data, view) {

        $(view).w2popup ('open', {

            width  : 605,
            height : 245,

            title   : 'Редактирование документа',

            onOpen: function (event) {

                event.onComplete = function () {
                                
                    $('#file_label').text (data.label)                    

                    var name = 'public_property_contract_docs_popup_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2reform ({

                        name: name,

                        record: data,

                        fields : [
                            {name: 'id_type', type: 'list', options: {items: $('body').data ('data').vc_pp_ctr_file_types.items}},
                            {name: 'protocolnum', type: 'text'},
                            {name: 'protocoldate', type: 'date'},
                            {name: 'description',  type: 'textarea' },
                        ],
                        
                        focus: -1,

                        onChange: function (e) {
                                                    
                            if (e.target == "id_type") {
                                $('#proto').css ({visibility: e.value_new.id == 3 ? 'visible' : 'hidden'})
                            }
                            
                        },
                        
                        onRender: function (e) {
                            e.done (function (e) {
                                $('#proto').css ({visibility: data.id_type == 3 ? 'visible' : 'hidden'})
                            })
                        }

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_public_property_contract_docs_popup)
                    clickOn ($('#file_label'), $_DO.download_public_property_contract_docs_popup)

                }

            }

        })

    }    

});