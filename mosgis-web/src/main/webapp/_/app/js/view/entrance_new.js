define ([], function () {

    return function (data, view) {

        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'entrance_new_popup_form',

                record: {},

                fields : [                
                    {name: 'no', type: 'text'},
                ],

//                focus: 1,

            })

            clickOn ($('#w2ui-popup button'), $_DO.update_entrance_new)

       })

    }

})