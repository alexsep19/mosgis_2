define ([], function () {

    $_DO.update_house_voting_protocols_new = function (e) {

        var form = w2ui ['house_voting_protocols_new_form']

        var v = form.values ()
        
        if (!v.protocoldate) die ('protocoldate', 'Пожалуйста, введите дату составления протокола')
        if (!v.protocolform) die ('protocolform', 'Пожалуйста, выберите форму проведения')
        
        switch (v.protocolform) {
            case '0': {
                    if (!v.avotingdate) die ('avotingdate', 'Пожалуйста')
                    if (!v.resolutionplace) die ('resolutionplace', 'Пожалуйста')
                    break
            }
            case '1': {
                    if (!v.meetingdate) die ('meetingdate', 'Пожалуйста')
                    if (!v.votingplace) die ('votingplace', 'Пожалуйста')
                    break
            }
            case '2': {
                    if (!v.evotingdatebegin) die ('evotingdatebegin', 'Пожалуйста')
                    if (!v.evotingdateend) die ('evotingdateend', 'Пожалуйста')
                    if (!v.discipline) die ('discipline', 'Пожалуйста')
                    if (!v.inforeview) die ('inforeview', 'Пожалуйста')
                    break
            }
            case '3': {
                    if (!v.meeting_av_date) die ('meeting_av_date', 'Пожалуйста')
                    if (!v.meeting_av_date_end) die ('meeting_av_date_end', 'Пожалуйста')
                    if (!v.meeting_av_place) die ('meeting_av_place', 'Пожалуйста')
                    if (!v.meeting_av_res_place) die ('meeting_av_res_place', 'Пожалуйста')
                    break
            }
        }
        
        var tia = {type: 'voting_protocols'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['house_voting_protocols_grid']

        query (tia, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу протокола?').yes (function () {openTab ('/voting_protocol/' + data.id)})
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record')
        
        done (data)

    }

})