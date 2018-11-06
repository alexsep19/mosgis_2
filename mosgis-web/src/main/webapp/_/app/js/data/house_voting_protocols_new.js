define ([], function () {

    $_DO.update_house_voting_protocols_new = function (e) {

        var form = w2ui ['house_voting_protocols_new_form']

        var v = form.values ()
        
        console.log (v)
        
        if (!v.protocoldate) die ('protocoldate', 'Пожалуйста, введите дату составления протокола')
        if (!v.protocolform) die ('protocolform', 'Пожалуйста, выберите форму проведения')
        
        switch (v.protocolform) {
            case '0': {
                    if (!v.avotingdate) die ('avotingdate', 'Пожалуйста, введите дату окончания приема решений')
                    if (!v.resolutionplace) die ('resolutionplace', 'Пожалуйста, введите место приема решения')
                    break
            }
            case '1': {
                    if (!v.meetingdate) die ('meetingdate', 'Пожалуйста, дату и время проведения собрания')
                    if (!v.votingplace) die ('votingplace', 'Пожалуйста, место проведения собрания')
                    break
            }
            case '2': {
                    if (!v.evotingdatebegin) die ('evotingdatebegin', 'Пожалуйста, введите период проведения голосования')
                    if (!v.evotingdateend) die ('evotingdateend', 'Пожалуйста, введите период проведения голосования')
                    if (!v.discipline) die ('discipline', 'Пожалуйста, введите порядок приема оформленных в письменной форме решений собственников')
                    if (!v.inforeview) die ('inforeview', 'Пожалуйста, порядок ознакомления с информацией')
                    break
            }
            case '3': {
                    if (!v.meeting_av_date) die ('meeting_av_date', 'Пожалуйста, введите дату и время проведения собрания')
                    if (!v.meeting_av_date_end) die ('meeting_av_date_end', 'Пожалуйста, введите дату окончания приема решений')
                    if (!v.meeting_av_place) die ('meeting_av_place', 'Пожалуйста, введите место проведения собрания')
                    if (!v.meeting_av_res_place) die ('meeting_av_res_place', 'Пожалуйста, введите место приема решения')
                    break
            }
        }
        
        v['meetingeligibility'] = v['meetingeligibility'] == 0 ? "C" : "N"
        
        console.log (v)
        
        var tia = {type: 'voting_protocols'}
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        var done = reload_page

        var grid = w2ui ['house_voting_protocols_grid']
        
        var data = clone ($('body').data ('data'))
        
        v['fiashouseguid'] = data.item.fiashouseguid

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