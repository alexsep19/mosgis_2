define ([], function () {

    return function (data, view) {
        
        var name = 'house_voting_protocols_new_form'
        
        function recalc () {

            var v = w2ui [name].values ()

            var on = v.form_ == 0 ? 1 : 0

            var $avoitingdate = $('#avoitingdate')
            var $avoitingdate_row = $avoitingdate.closest ('.w2ui-field')
            
            var $resolutionplace = $('#resolutionplace')
            var $resolutionplace_row = $resolutionplace.closest ('.w2ui-field')

            if (on) {
                $avoitingdate_row.show ()
                $resolutionplace_row.show ()
                $avoitingdate.prop ('disabled', false).focus ()
                $resolutionplace.prop ('disabled', false)
            }
            else {
                $avoitingdate.val ('').prop ('disabled', true)
                $resolutionplace.val ('').prop ('disabled', true)
                $avoitingdate_row.hide ()
                $resolutionplace_row.hide ()
            }
                        
            var o = {
                form: 340,
                page: 340,
                //box: 216,
                popup: 400,
                //'form-box': 193,
            }
            
            for (var k in o) {
                $avoitingdate_row.closest ('.w2ui-' + k).height (o [k] + 30 * on)
                $resolutionplace_row.closest ('.w2ui-' + k).height (o [k] + 30 * on)
            }            
        }
        
        $(view).w2popup('open', {

            width  : 550,
            height : 390,

            title   : 'Добавление протокола',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'house_voting_protocols_new_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [                                
                            {name: 'protocolnum', type: 'text'},
                            {name: 'protocoldate', type: 'date'},
                    
                            {name: 'extravoting', type: 'list', options: { items: [
                                {id: 0, text: "Ежегодное"},
                                {id: 1, text: "Внеочередное"},
                            ]}},
                            {name: 'meetingeligibility', type: 'list', options: { items: [
                                {id: 0, text: "Правомочное"},
                                {id: 1, text: "Неправомочное"},
                            ]}},
                            
                            {name: 'form_', type: 'list', options: { items: [
                                {id: 0, text: "Заочное голосование (опросным путем)"},
                                {id: 1, text: "Очное голосование"},
                                {id: 2, text: "Заочное голосование с использованием системы"},
                                {id: 3, text: "Очно-заочное голосование"},
                            ]}},
                            
                            {name: 'avoitingdate', type: 'date'},
                            {name: 'resolutionplace', type: 'text'},
                        ],
                        
                        onChange: function (e) {if (e.target == "form_") e.done (recalc)},
                        
                        onRender: function (e) {e.done (setTimeout (recalc, 100))}

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_house_voting_protocols_new)

                }
                
            }
            
        });
    
    }
    
    
});