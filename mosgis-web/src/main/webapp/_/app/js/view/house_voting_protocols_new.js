define ([], function () {

    return function (data, view) {
        
        var name = 'house_voting_protocols_new_form'
        
        function disabling (element, i, arr) {
            element.val ('').prop ('disabled', true)
            element.closest ('.w2ui-field').hide ()
        }
        
        function enabling (element, i, arr) {
            element.closest ('.w2ui-field').show ()
            element.val ('').prop ('disabled', false)
        }
        
        function sizing (element, arr) {
            for (item in arr){
                element.closest ('.w2ui-' + item).height (arr[item])
            }
        }
        
        function set_size (elements, sizes) {
            
        }
        
        function recalc () {

            var items = {'avoting': [$('#avoitingdate'),
                                     $('#resolutionplace')],
                         'meeting': [$('#meetingdate'),
                                     $('#meetingtime'),
                                     $('#votingplace')],
                         'evoting': [$('#evotingdatebegin'),
                                     $('#evotingtimebegin'),
                                     $('#evotingdateend'),
                                     $('#evotingtimeend'),
                                     $('#discipline'),
                                     $('#inforeview')],
                         'meet_av': [$('#meeting_av_date'),
                                     $('#meeting_av_time'),
                                     $('#meeting_av_date_end'),
                                     $('#meeting_av_res_place')]}

            var sizes = {'avoting': {form: 290,
                                     page: 270,
                                     box: 330,
                                     popup: 340},
                         'meeting': {form: 330,
                                     page: 310,
                                     box: 360,
                                     popup: 380},
                         'evoting': {form: 435,//
                                     page: 415,//
                                     box: 455,//
                                     popup: 485,
                                     'form-box': 470},
                         'meet_av': {form: 364,
                                     page: 345,
                                     box: 390,
                                     popup: 415,
                                     'form-box': 370}}

            items['avoting'].forEach(disabling);
            items['meeting'].forEach(disabling);
            items['evoting'].forEach(disabling);
            items['meet_av'].forEach(disabling);

            var v = w2ui [name].values ()

            switch (v.form_) {
                case 0:
                    items['avoting'].forEach(enabling);
                    sizing (items['avoting'][0], sizes['avoting']);
                    break;
                case 1:
                    items['meeting'].forEach(enabling);
                    sizing (items['meeting'][0], sizes['meeting']);
                    break;
                case 2:
                    items['evoting'].forEach(enabling);
                    sizing (items['evoting'][0], sizes['evoting']);
                    break;
                case 3:
                    items['meet_av'].forEach(enabling);
                    sizing (items['meet_av'][0], sizes['meet_av']);
                    break;
            }
                        
//            var o = {
//                form: 340,
//                page: 340,
//                //box: 216,
//                popup: 400,
//                //'form-box': 193,
//            }
//            
//            for (var k in o) {
//                $avoitingdate_row.closest ('.w2ui-' + k).height (o [k] + 30 * on)
//                $resolutionplace_row.closest ('.w2ui-' + k).height (o [k] + 30 * on)
//            }            
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