define ([], function () {

    return function (data, view) {

        var name = 'metering_value_popup_form'
        
        var now = dt_dmy (new Date ().toJSON ())
        
        var delta = 30 * (data.item.tariffcount - 1)

        $(fill (view, data.record)).w2popup ('open', {

            width  : 340,
            height : 225 + delta,

            title   : data.resources [data.record.code_vc_nsi_2],

            onOpen: function (event) {

                event.onComplete = function () {

                    $('#w2ui-popup .w2ui-form').w2reform ({

                        name: name,
                        
                        record: data.record,

                        fields : [
                            {name: 'id_type', type: 'list', options: {items: data.vc_meter_value_types.items}},
                            {name: 'datevalue', type: 'date', options: {end: now}},
                            {name: 'meteringvaluet1', type: 'float', options: {precision: 7}},
                            {name: 'meteringvaluet2', type: 'float', options: {precision: 7}},
                            {name: 'meteringvaluet3', type: 'float', options: {precision: 7}},
                        ],
                        
                        focus: 2,
                                                
                    });
                    
//                    $('#poppupp').height (175 + delta)

                    clickOn ($('#w2ui-popup button'), $_DO.update_metering_value_popup)

                }

            }

        });

    }

});