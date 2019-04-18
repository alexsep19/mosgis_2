define ([], function () {

    var name = 'overhaul_address_program_houses_new_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [ 
                    {name: 'house', type: 'hidden'},
                    {name: 'house_address', type: 'text'},
                ],

                onRefresh: function (e) {e.done (function () {
                
                    clickOn ($('#house_address'), $_DO.open_houses_popup)
                
                })}
                                                        
            })

       })

    }

})