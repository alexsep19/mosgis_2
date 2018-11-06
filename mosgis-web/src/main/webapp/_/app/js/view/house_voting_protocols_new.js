define ([], function () {

    return function (data, view) {
        
        $(view).w2popup('open', {

            width  : 605,
            height : 280,

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
                    
                            {name: 'extravoting', type: 'list', options: {items: [{id:0, text: "Ежегодное"}, {id:1, text: "Внеочередное"}]}},
                            {name: 'meetingeligibility', type: 'list', options: {items: [{id:"C", text: "Правомочно"}, {id:"N", text: "Неправомочно"}]}},
                            
                            {name: 'protocolform', type: 'radio'},
                    
                            {name: 'avotingdate', type: 'date'},
                            {name: 'resolutionplace', type: 'text'},

                            {name: 'meetingdate', type: 'date'},
                            {name: 'votingplace', type: 'text'},

                            {name: 'evotingdatebegin', type: 'date'},
                            {name: 'evotingdateend', type: 'date'},
                            {name: 'discipline', type: 'text'},
                            {name: 'inforeview', type: 'text'},

                            {name: 'meeting_av_date', type: 'date'},
                            {name: 'meeting_av_date_end', type: 'date'},
                            {name: 'meeting_av_place', type: 'text'},
                            {name: 'meeting_av_res_place', type: 'text'},
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_house_voting_protocols_new)

                }
                
            }
            
        });
    
    }
    
    
});