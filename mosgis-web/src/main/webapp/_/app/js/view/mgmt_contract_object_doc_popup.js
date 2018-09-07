define ([], function () {

    return function (data, view) {

        var have_purchasenumber = (data.id_type == 9)

        $(view).w2popup ('open', {

            width  : 625,
            height : 220,

            title   : 'Редактирование документа',

            onOpen: function (event) {

                event.onComplete = function () {
                                
                    $('#file_label').text (data.label)                    

                    var name = 'mgmt_contract_object_doc_popup_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        record: data,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                        ],

                        focus: -1

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_mgmt_contract_object_doc_popup)
                    clickOn ($('#file_label'), $_DO.download_mgmt_contract_object_doc_popup)
                    
                    if (!have_purchasenumber) $('#purchasenumber').closest ('.w2ui-field').hide ()

                }

            }

        })

    }    

});