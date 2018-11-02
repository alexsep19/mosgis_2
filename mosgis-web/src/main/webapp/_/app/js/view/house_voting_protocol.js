define ([], function () {
    
    var form_name = 'house_voting_protocol_form'

    return function (data, view) {
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
        
        fill (view, data.item, $panel)        

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,
            
            fields : [          
            
                {name: 'protocolnum', type: 'text'},
                {name: 'protocoldate', type: 'date'},
                {name: 'votingtype', type: 'list', options: {items: [{id: "0", text: "Внеочередное"}, {id: "1", text: "Очередное"}]}},
                {name: 'votingform', type: 'radio'},
                
                {name: 'avoitingdate', type: 'date'},
                {name: 'resolutionplace', type: 'text'},
                
                {name: 'meetingdate', type: 'date'},
                {name: 'votingplace', type: 'text'},
                
                {name: 'evotingdatebegin', type: 'date'},
                {name: 'evotingdateend', type: 'date'},
                {name: 'discipline', type: 'text'},
                {name: 'inforeview', type: 'text'},
                
                {name: 'meeting_av_date', type: 'date'},
                {name: 'meeting_av_place', type: 'text'},
                {name: 'meeting_av_date_end', type: 'date'},
                {name: 'meeting_av_res_place', type: 'text'},
                            
            ].filter (not_off),
            
            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})